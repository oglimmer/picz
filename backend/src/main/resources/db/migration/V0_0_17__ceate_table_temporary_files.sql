CREATE TABLE temporary_files
(
    id        BIGINT       NOT NULL AUTO_INCREMENT,
    unique_id VARCHAR(255) NOT NULL,
    content   LONGBLOB,
    PRIMARY KEY (id),
    UNIQUE KEY uq_temporary_files_unique_id (unique_id)
);