package de.oglimmer.picz.db;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "job_queue",
        indexes = @Index(name = "idx_job_queue_processor_status_next_run", columnList = "processorName, status, nextRun"))
@Getter
@Setter
@NoArgsConstructor
public class JobQueue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payload_json", nullable = false, columnDefinition = "LONGTEXT")
    private String payloadJson;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatus status = JobStatus.PENDING;

    @Column(name = "locked_by", length = 64)
    private String lockedBy;

    @Column(name = "locked_at")
    private Instant lockedAt;

    @Column(name = "next_run", nullable = false)
    private Instant nextRun;

    @Column(name = "processor_name", nullable = false, length = 64)
    private String processorName;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "jobQueue", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<JobResult> results;

    public enum JobStatus {
        PENDING, IN_PROGRESS, COMPLETED, FAILED
    }

    public JobQueue(String payloadJson, Instant nextRun, String processorName) {
        this.payloadJson = payloadJson;
        this.nextRun = nextRun;
        this.processorName = processorName;
    }
}