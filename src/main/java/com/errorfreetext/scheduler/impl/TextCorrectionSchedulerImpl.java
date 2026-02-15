package com.errorfreetext.scheduler.impl;


import com.errorfreetext.dto.CorrectionResult;
import com.errorfreetext.entity.Task;
import com.errorfreetext.scheduler.TextCorrectionScheduler;
import com.errorfreetext.service.TaskService;
import com.errorfreetext.service.TextCorrectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Scheduler implementation for automatic text correction processing.
 * Handles two main scheduled tasks:
 * <ul>
 *   <li>Processing pending text correction tasks at regular intervals</li>
 *   <li>Recovering tasks that have been stuck in PROCESSING state for too long</li>
 * </ul>
 *
 * This component runs asynchronously in the background and manages the task lifecycle
 * from PENDING → PROCESSING → COMPLETED/FAILED.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TextCorrectionSchedulerImpl implements TextCorrectionScheduler {

    private final TaskService taskService;
    private final TextCorrectionService correctionService;

    /**
     * Processes pending text correction tasks at a fixed interval.
     * <p>
     * This method:
     * <ul>
     *   <li>Fetches up to 10 pending tasks (oldest first)</li>
     *   <li>For each task, transitions it to PROCESSING state</li>
     *   <li>Performs text correction via {@link TextCorrectionService}</li>
     *   <li>If successful, marks task as COMPLETED with corrected text</li>
     *   <li>If failed, marks task as FAILED with error message</li>
     * </ul>
     * </p>
     *
     * The fixed delay is configurable via the 'scheduler.fixed-delay' property,
     * defaulting to 30000 milliseconds (30 seconds) if not specified.
     * Each task is processed in a separate transaction to ensure that failures
     * in one task don't affect others.
     */
    @Scheduled(fixedDelayString = "${scheduler.fixed-delay:30000}")
    @Transactional
    @Override
    public void processPendingTasks() {
        log.debug("Starting scheduled task processing");
        List<Task> pendingTasks = taskService.findPendingTasks(10);

        if (pendingTasks.isEmpty()) {
            log.debug("No pending tasks found");
            return;
        }
        log.info("Found {} pending tasks to process", pendingTasks.size());

        for (Task task : pendingTasks) {
            processSingleTask(task);
        }
        log.debug("Completed scheduled task processing");
    }

    /**
     * Recovers tasks that have been stuck in PROCESSING state for an extended period.
     * <p>
     * Tasks may become stuck due to:
     * <ul>
     *   <li>Application crashes during processing</li>
     *   <li>Network timeouts when calling external APIs</li>
     *   <li>Unexpected exceptions that weren't properly caught</li>
     * </ul>
     * </p>
     *
     * This recovery mechanism:
     * <ul>
     *   <li>Identifies tasks in PROCESSING state that haven't been updated for >30 minutes</li>
     *   <li>Marks them as FAILED with a timeout error message</li>
     *   <li>Allows these tasks to be retried or investigated manually</li>
     * </ul>
     *
     * The check interval is configurable via the 'scheduler.stuck-check-delay' property,
     * defaulting to 300000 milliseconds (5 minutes) if not specified.
     */
    @Scheduled(fixedDelayString = "${scheduler.stuck-check-delay:300000}")
    @Transactional
    @Override
    public void recoverStuckTasks() {
        log.info("Checking for stuck tasks...");
        List<Task> stuckTasks = taskService.findStuckTasks(30);
        for (Task task : stuckTasks) {
            log.warn("Recovering stuck task: {}", task.getId());
            taskService.markAsFailed(
                    task.getId(),
                    "Task processing timeout after 30 minutes"
            );
        }
    }

    /**
     * Processes a single task through its correction lifecycle.
     * This method encapsulates the try-catch logic for individual task processing
     * to ensure clean separation of concerns.
     *
     * @param task the task to process
     */
    private void processSingleTask(Task task) {
        log.debug("Processing task: {}", task.getId());
        try {
            taskService.markAsProcessing(task.getId());
            CorrectionResult result = correctionService.correctText(task);
            taskService.markAsCompleted(
                    task.getId(),
                    result.getCorrectedText(),
                    result.getOptions()
            );
            log.info("Task {} processed successfully", task.getId());
        } catch (Exception e) {
            log.error("Failed to process task {}", task.getId(), e);
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Unknown error";
            taskService.markAsFailed(task.getId(), errorMessage);
        }
    }
}