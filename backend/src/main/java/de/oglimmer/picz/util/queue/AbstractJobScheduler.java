/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.picz.util.queue;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.oglimmer.picz.db.*;
import jakarta.transaction.Transactional;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

@AllArgsConstructor
@Slf4j
public abstract class AbstractJobScheduler {

  protected final String instanceId = UUID.randomUUID().toString();

  protected final JobQueueRepository jobQueueRepository;
  protected final JobResultRepository jobResultRepository;
  protected final MariaDBLockService mariaDBLockService;
  protected final ApplicationContext applicationContext;
  protected final ObjectMapper objectMapper;

  protected abstract String getLockName();

  protected String getSchedulerName() {
    return this.getClass().getSimpleName();
  }

  protected abstract void processScheduledJobs();

  protected List<JobQueue> findDueJobs() {
    return jobQueueRepository.findDueJobs(Instant.now(), JobQueue.JobStatus.PENDING, getLockName());
  }

  protected boolean claimJob(JobQueue job) {
    int updated =
        jobQueueRepository.updateJobStatusIfMatch(
            job.getId(),
            JobQueue.JobStatus.IN_PROGRESS,
            instanceId,
            Instant.now(),
            JobQueue.JobStatus.PENDING);
    return updated > 0;
  }

  @SneakyThrows
  @Transactional(Transactional.TxType.REQUIRES_NEW)
  public String processJobInTransaction(String payloadJson) {
    MethodInvocationPayload payload =
        objectMapper.readValue(payloadJson, MethodInvocationPayload.class);

    Object targetBean = applicationContext.getBean(payload.beanName());

    String[] typeNames = payload.parameterTypeNames();
    Class<?>[] paramTypes = new Class<?>[typeNames.length];
    for (int i = 0; i < typeNames.length; i++) {
      paramTypes[i] = Class.forName(typeNames[i]);
    }

    Object[] rawArgs = payload.args();
    Object[] typedArgs = new Object[rawArgs.length];
    for (int i = 0; i < rawArgs.length; i++) {
      typedArgs[i] = objectMapper.convertValue(rawArgs[i], paramTypes[i]);
    }

    Method method = targetBean.getClass().getMethod(payload.methodName(), paramTypes);
    //            method.setAccessible(true);

    Object returnValue = method.invoke(targetBean, typedArgs);

    if (method.getReturnType() != Void.TYPE) {
      return objectMapper.writeValueAsString(returnValue);
    }

    return null;
  }

  protected void onSuccess(JobQueue job, JobQueue.JobStatus completed, JobResult result) {
    jobQueueRepository.updateJobStatus(job.getId(), completed);
    jobResultRepository.save(result);
  }

  protected void onFailed(Throwable throwable, JobQueue job) {
    onSuccess(
        job, JobQueue.JobStatus.FAILED, new JobResult(job, null, getStackTraceAsString(throwable)));
  }

  protected String getStackTraceAsString(Throwable t) {
    StringBuilder sb = new StringBuilder();
    sb.append(t.toString()).append("\n");
    for (StackTraceElement elem : t.getStackTrace()) {
      sb.append("\tat ").append(elem.toString()).append("\n");
    }
    if (t.getCause() != null) {
      sb.append("Caused by: ").append(getStackTraceAsString(t.getCause()));
    }
    return sb.toString();
  }
}
