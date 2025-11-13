-- Add processor_name column to job_queue table
ALTER TABLE job_queue
ADD COLUMN processor_name VARCHAR(64) NOT NULL DEFAULT 'MAIN_SCHEDULER';

-- Update the index to include processor_name for better query performance
DROP INDEX idx_job_queue_status_next_run ON job_queue;
CREATE INDEX idx_job_queue_processor_status_next_run ON job_queue (processor_name, status, next_run);
