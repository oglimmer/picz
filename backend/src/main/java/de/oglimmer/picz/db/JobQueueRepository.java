package de.oglimmer.picz.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Repository
public interface JobQueueRepository extends JpaRepository<JobQueue, Long> {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    JobQueue save(JobQueue entity);

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Query("SELECT j FROM JobQueue j WHERE j.nextRun <= :now AND j.status = :status AND j.processorName = :processorName " +
            "ORDER BY j.nextRun ASC")
    List<JobQueue> findDueJobs(@Param("now") Instant now, @Param("status") JobQueue.JobStatus status, @Param("processorName") String processorName);

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Modifying
    @Query("UPDATE JobQueue j SET j.status = :status, j.lockedBy = :lockedBy, j.lockedAt = :lockedAt " +
            "WHERE j.id = :jobId AND j.status = :currentStatus")
    int updateJobStatusIfMatch(@Param("jobId") Long jobId,
                               @Param("status") JobQueue.JobStatus status,
                               @Param("lockedBy") String lockedBy,
                               @Param("lockedAt") Instant lockedAt,
                               @Param("currentStatus") JobQueue.JobStatus currentStatus);

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Modifying
    @Query("UPDATE JobQueue j SET j.status = :status WHERE j.id = :jobId")
    void updateJobStatus(@Param("jobId") Long jobId, @Param("status") JobQueue.JobStatus status);

}