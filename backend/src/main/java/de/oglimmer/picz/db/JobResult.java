package de.oglimmer.picz.db;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "job_results")
@Getter
@Setter
@NoArgsConstructor
public class JobResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false, foreignKey = @ForeignKey(name = "fk_job_results_job"))
    private JobQueue jobQueue;

    @Column(name = "result_json", columnDefinition = "LONGTEXT")
    private String resultJson;

    @Column(name = "exception_text", columnDefinition = "TEXT")
    private String exceptionText;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public JobResult(JobQueue jobQueue, String resultJson, String exceptionText) {
        this.jobQueue = jobQueue;
        this.resultJson = resultJson;
        this.exceptionText = exceptionText;
    }
}