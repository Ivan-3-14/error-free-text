package com.errorfreetext.scheduler;

import com.errorfreetext.service.TextCorrectionService;

import java.util.List;
import java.util.UUID;

/**
 * Scheduler interface for automatic text correction processing.
 * <p>
 * Defines the contract for scheduled tasks that manage the lifecycle
 * of text correction jobs. Implementations of this interface are
 * automatically discovered by Spring's scheduling mechanism and
 * executed at configured intervals.
 * </p>
 *
 * <p>
 * The scheduler is responsible for:
 * <ul>
 *   <li>Fetching pending tasks from the database</li>
 <li>Initiating text correction for each pending task</li>
 *   <li>Handling task state transitions (PENDING → PROCESSING → COMPLETED/FAILED)</li>
 *   <li>Ensuring fault tolerance through proper error handling</li>
 * </ul>
 * </p>
 *
 * <p>
 * <b>Note:</b> This interface is designed to work with Spring's {@code @Scheduled}
 * annotation. Methods are called automatically by the Spring scheduler
 * without any manual invocation.
 * </p>
 *
 * @see org.springframework.scheduling.annotation.Scheduled
 * @see com.errorfreetext.scheduler.impl.TextCorrectionSchedulerImpl
 * @see com.errorfreetext.service.TaskService
 * @see com.errorfreetext.service.TextCorrectionService
 */
public interface TextCorrectionScheduler {

    /**
     * Processes all pending text correction tasks.
     * <p>
     * This method is automatically invoked at regular intervals configured
     * via the {@code @Scheduled} annotation. It performs the following steps:
     * <ol>
     *   <li>Retrieves a batch of pending tasks (oldest first)</li>
     *   <li>For each task, marks it as PROCESSING to prevent duplicate processing</li>
     *   <li>Initiates text correction via {@link TextCorrectionService}</li>
     *   <li>Updates task status to COMPLETED on success</li>
     *   <li>Updates task status to FAILED with error message on exception</li>
     * </ol>
     * </p>
     *
     * <p>
     * <b>Scheduling configuration:</b>
     * <pre>
     * {@code @Scheduled(fixedDelayString = "${scheduler.fixed-delay:30000}")}
     * </pre>
     * Default delay is 30 seconds, configurable via {@code scheduler.fixed-delay}
     * property in application configuration.
     * </p>
     *
     * <p>
     * <b>Error handling:</b>
     * Individual task failures are caught and logged, with the task marked as FAILED.
     * This ensures that one failing task doesn't prevent others from being processed.
     * </p>
     *
     * @apiNote This method should never be called manually. It is designed to be
     *          invoked automatically by Spring's scheduled task executor.
     *
     * @see com.errorfreetext.entity.enums.TaskStatus
     * @see com.errorfreetext.service.TaskService#findPendingTasks(int)
     * @see com.errorfreetext.service.TaskService#markAsProcessing(UUID)
     * @see com.errorfreetext.service.TaskService#markAsCompleted(UUID, String, List)
     * @see com.errorfreetext.service.TaskService#markAsFailed(UUID, String)
     */
    void processPendingTasks();

    /**
     * Recovers tasks that have been stuck in PROCESSING state for an extended period.
     * <p>
     * This method is automatically invoked at regular intervals to identify and recover
     * tasks that may have been abandoned due to application crashes, network timeouts,
     * or unexpected errors during text correction.
     * </p>
     *
     * <p>
     * <b>Why tasks get stuck:</b>
     * <ul>
     *   <li>Application crash or restart during processing</li>
     *   <li>Network timeout when calling Yandex Speller API</li>
     *   <li>Unhandled exceptions that leave tasks in PROCESSING state</li>
     *   <li>Scheduler thread interruptions</li>
     * </ul>
     * </p>
     *
     * <p>
     * <b>Recovery process:</b>
     * <ol>
     *   <li>Identifies tasks in PROCESSING state that haven't been updated
     *       for more than the configured timeout period (default: 30 minutes)</li>
     *   <li>For each stuck task, marks it as FAILED with a timeout error message</li>
     *   <li>This allows the tasks to be retried or investigated manually</li>
     * </ol>
     * </p>
     *
     * <p>
     * <b>Scheduling configuration:</b>
     * <pre>
     * {@code @Scheduled(fixedDelayString = "${scheduler.stuck-check-delay:300000}")}
     * </pre>
     * Default check interval is 5 minutes (300000 ms), configurable via
     * {@code scheduler.stuck-check-delay} property in application configuration.
     * </p>
     *
     * <p>
     * <b>Timeout configuration:</b>
     * The method considers tasks as stuck if they have been in PROCESSING state
     * for more than 30 minutes. This timeout is hardcoded as it represents a
     * business rule: text correction should never take this long under normal
     * circumstances.
     * </p>
     *
     * <p>
     * <b>Example scenario:</b>
     * <pre>
     * 1. Task enters PROCESSING state at 12:00:00
     * 2. Application crashes at 12:00:05
     * 3. Application restarts at 12:01:00
     * 4. Recovery scheduler runs at 12:05:00
     * 5. Task is detected as stuck (last update: 12:00:00)
     * 6. Task is marked as FAILED with message: "Task processing timeout after 30 minutes"
     * </pre>
     * </p>
     *
     * @apiNote This method should never be called manually. It is designed to be
     *          invoked automatically by Spring's scheduled task executor.
     *
     * @see com.errorfreetext.entity.enums.TaskStatus#PROCESSING
     * @see com.errorfreetext.service.TaskService#findStuckTasks(long)
     * @see com.errorfreetext.service.TaskService#markAsFailed(UUID, String)
     * @see org.springframework.scheduling.annotation.Scheduled
     */
    void recoverStuckTasks();
}