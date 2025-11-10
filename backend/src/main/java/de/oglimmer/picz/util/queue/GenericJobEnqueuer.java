package de.oglimmer.picz.util.queue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.oglimmer.picz.db.JobQueue;
import de.oglimmer.picz.db.JobQueueRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * A service to enqueue “bean.method(...)” jobs.
 * Internally serializes MethodInvocationPayload to JSON and stores it in job_queue.
 */
@AllArgsConstructor
@Service
@Transactional(Transactional.TxType.REQUIRES_NEW)
public class GenericJobEnqueuer {

    private final JobQueueRepository jobQueueRepository;
    private final ObjectMapper objectMapper;

    /**
     * Enqueue a method invocation at a specific runTime.
     *
     * @param beanName           Spring bean name (must exist in context)
     * @param methodName         the method to call on that bean
     * @param parameterTypeNames fully-qualified class names, in order
     * @param args               argument values, matching parameter types
     * @param runTime            when to run (Instant). If <= NOW(), scheduler picks it next tick.
     * @param processorName      which scheduler should process this job (MAIN_SCHEDULER or PARALLEL_SCHEDULER)
     */
    public void enqueue(
            String beanName,
            String methodName,
            String[] parameterTypeNames,
            Object[] args,
            Instant runTime,
            String processorName
    ) {
        // check processorName
        if (!"MAIN_SCHEDULER".equals(processorName) && !"PARALLEL_SCHEDULER".equals(processorName)) {
            throw new IllegalArgumentException("Invalid processor name: " + processorName);
        }

        MethodInvocationPayload payload = new MethodInvocationPayload(
                beanName, methodName, parameterTypeNames, args
        );

        String json;
        try {
            json = objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Cannot serialize MethodInvocationPayload to JSON", e);
        }

        JobQueue jobQueue = new JobQueue(json, runTime, processorName);
        jobQueueRepository.save(jobQueue);
    }

    /**
     * Enqueue for immediate execution (next_run = NOW()) with specified processor.
     */
    public void enqueueImmediately(
            String beanName,
            String methodName,
            String[] parameterTypeNames,
            Object[] args,
            String processorName
    ) {
        enqueue(beanName, methodName, parameterTypeNames, args, Instant.now(), processorName);
    }
}
