package com.errorfreetext.service;

import com.errorfreetext.dto.CreateTaskRequest;
import com.errorfreetext.dto.TaskResponse;
import com.errorfreetext.entity.Task;
import com.errorfreetext.exception.TaskNotFoundException;
import com.errorfreetext.exception.ValidationException;

import java.util.List;
import java.util.UUID;

/**
 * Service interface defining operations for managing text correction tasks.
 * <p>
 * This interface represents the contract for task lifecycle management,
 * from creation through processing to completion or failure. It provides
 * methods for retrieving tasks in various states and updating their status
 * during the correction process.
 * </p>
 *
 * <p>
 * The typical task lifecycle is:
 * <ol>
 *   <li><b>PENDING</b> - Task created, waiting for processing</li>
 *   <li><b>PROCESSING</b> - Task picked up by scheduler, correction in progress</li>
 *   <li><b>COMPLETED</b> - Text corrected successfully</li>
 *   <li><b>FAILED</b> - Error occurred during correction</li>
 * </ol>
 * </p>
 *
 * @see com.errorfreetext.entity.Task
 * @see com.errorfreetext.dto.TaskResponse
 */
public interface TaskService {

    /**
     * Creates a new text correction task.
     * <p>
     * Validates the input text, creates a task entity with PENDING status,
     * persists it to the database, and returns a response DTO with the
     * generated task ID.
     * </p>
     *
     * @param request the request containing the text to be corrected and its language
     * @return a {@link TaskResponse} containing the created task's details
     * @throws ValidationException if the text fails validation (e.g., only special characters)
     */
    TaskResponse createTask(CreateTaskRequest request);

    /**
     * Retrieves a task by its unique identifier.
     * <p>
     * Returns the current state of the task, including its status and,
     * if available, the corrected text.
     * </p>
     *
     * @param id the UUID of the task to retrieve
     * @return a {@link TaskResponse} containing the task's current state
     * @throws TaskNotFoundException if no task exists with the given ID
     */
    TaskResponse getTask(UUID id);

    /**
     * Retrieves a limited number of pending tasks for processing.
     * <p>
     * Used by the scheduler to fetch tasks that need text correction.
     * Tasks are ordered by creation date (oldest first) to ensure fair
     * processing order.
     * </p>
     *
     * @param limit the maximum number of tasks to retrieve
     * @return a list of {@link Task} entities with PENDING status
     */
    List<Task> findPendingTasks(int limit);

    /**
     * Identifies tasks that have been stuck in PROCESSING state for too long.
     * <p>
     * Tasks may become stuck due to application crashes, network timeouts,
     * or unexpected exceptions. This method helps recover them by finding
     * tasks that haven't been updated within the specified timeout period.
     * </p>
     *
     * @param timeoutMinutes the timeout threshold in minutes;
     *                      tasks updated before this time are considered stuck
     * @return a list of {@link Task} entities stuck in PROCESSING state
     */
    List<Task> findStuckTasks(long timeoutMinutes);

    /**
     * Marks a task as PROCESSING.
     * <p>
     * Updates the task status to indicate that text correction has started.
     * This prevents the same task from being picked up by other scheduler instances
     * or during subsequent scheduler runs.
     * </p>
     *
     * @param id the UUID of the task to mark as processing
     * @throws TaskNotFoundException if no task exists with the given ID
     */
    void markAsProcessing(UUID id);

    /**
     * Marks a task as COMPLETED with the corrected text.
     * <p>
     * Called when text correction finishes successfully. Updates the task with
     * the corrected text, applied options, and sets status to COMPLETED.
     * </p>
     *
     * @param id            the UUID of the task to mark as completed
     * @param correctedText the corrected version of the original text
     * @param options       the list of Yandex Speller options that were applied
     * @throws TaskNotFoundException if no task exists with the given ID
     */
    void markAsCompleted(UUID id, String correctedText, List<String> options);

    /**
     * Marks a task as FAILED with an error message.
     * <p>
     * Called when text correction encounters an unrecoverable error.
     * Stores the error message for debugging and sets status to FAILED.
     * </p>
     *
     * @param id           the UUID of the task to mark as failed
     * @param errorMessage a description of the error that occurred
     * @throws TaskNotFoundException if no task exists with the given ID
     */
    void markAsFailed(UUID id, String errorMessage);
}