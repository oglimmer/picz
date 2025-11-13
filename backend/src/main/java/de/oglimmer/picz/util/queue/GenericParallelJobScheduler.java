/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.picz.util.queue;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.oglimmer.picz.config.ParallelSchedulerProps;
import de.oglimmer.picz.db.*;
import jakarta.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GenericParallelJobScheduler extends AbstractJobScheduler {

  private static final String LOCK_NAME = "PARALLEL_SCHEDULER";
  private final AtomicInteger activeJobs = new AtomicInteger(0);

  private final ParallelSchedulerProps schedulerProps;
  private final ExecutorService executorService;

  public GenericParallelJobScheduler(
      JobQueueRepository jobQueueRepository,
      JobResultRepository jobResultRepository,
      MariaDBLockService schedulerLockRepository,
      ApplicationContext applicationContext,
      ObjectMapper objectMapper,
      ParallelSchedulerProps schedulerProps) {
    super(
        jobQueueRepository,
        jobResultRepository,
        schedulerLockRepository,
        applicationContext,
        objectMapper);
    this.schedulerProps = schedulerProps;
    this.executorService = Executors.newFixedThreadPool(schedulerProps.getMaxConcurrentJobs());
    log.info(
        "GenericParallelJobScheduler initialized with {} max concurrent jobs",
        schedulerProps.getMaxConcurrentJobs());
  }

  @Override
  protected String getLockName() {
    return LOCK_NAME;
  }

  @Scheduled(fixedDelay = 1_000)
  public void scheduleDueJobs() {
    Thread.currentThread().setName(getSchedulerName());

    processScheduledJobs();
  }

  @Override
  protected void processScheduledJobs() {
    int availableSlots = schedulerProps.getMaxConcurrentJobs() - activeJobs.get();
    if (availableSlots <= 0) {
      log.debug("No available slots for parallel execution. Active jobs: {}", activeJobs.get());
      return;
    }

    List<JobQueue> dueJobs = findDueJobs();
    do {
      int jobsToProcess = Math.min(availableSlots, dueJobs.size());
      if (jobsToProcess > 0) {
        log.debug(
            "Found {} due jobs, processing {} (available slots: {})",
            dueJobs.size(),
            jobsToProcess,
            availableSlots);
      }

      for (int i = 0; i < jobsToProcess; i++) {
        JobQueue job = dueJobs.get(i);

        if (claimJob(job)) {
          activeJobs.incrementAndGet();
          CompletableFuture.supplyAsync(
                  () -> processJobAsync(job.getPayloadJson()), executorService)
              .whenComplete(completeJob(job));
        }
      }
      dueJobs = findDueJobs();
    } while (!dueJobs.isEmpty() && activeJobs.get() < schedulerProps.getMaxConcurrentJobs());
  }

  private BiConsumer<String, Throwable> completeJob(JobQueue job) {
    return (result, throwable) -> {
      activeJobs.decrementAndGet();
      if (throwable != null) {
        log.error("Unexpected error in async job processing for job {}", job.getId(), throwable);
        onFailed(throwable, job);
      } else {
        onSuccess(job, JobQueue.JobStatus.COMPLETED, new JobResult(job, result, null));
      }
    };
  }

  private String processJobAsync(String payloadJson) {
    AbstractJobScheduler jobScheduler =
        applicationContext.getBean("genericParallelJobScheduler", AbstractJobScheduler.class);
    return jobScheduler.processJobInTransaction(payloadJson);
  }

  @PreDestroy
  public void shutdown() {
    log.info("Shutting down GenericParallelJobScheduler...");
    executorService.shutdown();
    try {
      if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
        log.warn("Executor did not terminate gracefully, forcing shutdown");
        executorService.shutdownNow();
      }
    } catch (InterruptedException e) {
      log.warn("Interrupted while waiting for executor termination");
      executorService.shutdownNow();
      Thread.currentThread().interrupt();
    }
    log.info("GenericParallelJobScheduler shutdown complete");
  }
}
