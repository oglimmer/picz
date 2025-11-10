

CREATE TABLE IF NOT EXISTS job_queue (
                                         id              BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                         payload_json    LONGTEXT        NOT NULL,
                                         status          ENUM('PENDING','IN_PROGRESS','COMPLETED','FAILED')
                                                                         NOT NULL DEFAULT 'PENDING',
                                         locked_by       VARCHAR(64)     NULL,
                                         locked_at       TIMESTAMP       NULL,
                                         next_run        TIMESTAMP       NOT NULL,
                                         created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                         updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
                                             ON UPDATE CURRENT_TIMESTAMP,
                                         INDEX idx_job_queue_status_next_run (status, next_run)
);


CREATE TABLE IF NOT EXISTS job_results (
                                           id              BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                           job_id          BIGINT          NOT NULL,
                                           result_json     LONGTEXT        NULL,
                                           exception_text  TEXT            NULL,
                                           created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                           CONSTRAINT fk_job_results_job
                                               FOREIGN KEY (job_id) REFERENCES job_queue(id)
                                                   ON DELETE CASCADE
);
