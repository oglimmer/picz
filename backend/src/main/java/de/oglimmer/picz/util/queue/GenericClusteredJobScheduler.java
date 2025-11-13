/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.picz.util.queue;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.oglimmer.picz.db.*;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * This scheduler executes jobs a single-threaded manner, ensuring that only one instance of the job
 * is active at a time across the cluster.
 */
@Slf4j
@Component
public class GenericClusteredJobScheduler extends AbstractJobScheduler {

  private static final String LOCK_NAME = "MAIN_SCHEDULER";

  public GenericClusteredJobScheduler(
      JobQueueRepository jobQueueRepository,
      JobResultRepository jobResultRepository,
      MariaDBLockService schedulerLockRepository,
      ApplicationContext applicationContext,
      ObjectMapper objectMapper) {
    super(
        jobQueueRepository,
        jobResultRepository,
        schedulerLockRepository,
        applicationContext,
        objectMapper);
  }

  @Override
  protected String getLockName() {
    return LOCK_NAME;
  }

  @Scheduled(fixedDelay = 1_000)
  public void scheduleDueJobs() {
    Thread.currentThread().setName(getSchedulerName());

    mariaDBLockService.doWithLock(getLockName(), this::processScheduledJobs);
  }

  @Override
  protected void processScheduledJobs() {
    List<JobQueue> dueJobs = findDueJobs();
    do {
      for (JobQueue job : dueJobs) {
        if (claimJob(job)) {
          try {
            AbstractJobScheduler jobScheduler =
                applicationContext.getBean(
                    "genericClusteredJobScheduler", AbstractJobScheduler.class);
            String result = jobScheduler.processJobInTransaction(job.getPayloadJson());
            onSuccess(job, JobQueue.JobStatus.COMPLETED, new JobResult(job, result, null));
          } catch (Throwable throwable) {
            log.error(
                "Unexpected error in async job processing for job {}", job.getId(), throwable);
            onFailed(throwable, job);
          }
        }
      }
      dueJobs = findDueJobs();
    } while (!dueJobs.isEmpty());
  }
}
