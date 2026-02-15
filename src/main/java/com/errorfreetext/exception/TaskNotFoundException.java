package com.errorfreetext.exception;

import com.errorfreetext.dto.ErrorResponse;
import lombok.Getter;

import java.util.UUID;

/**
 * Exception thrown when a requested task cannot be found in the database.
 * <p>
 * This exception is used throughout the service layer to indicate that
 * an operation failed because the specified task ID does not exist.
 * It integrates with the global exception handler to produce consistent
 * error responses in the {@link ErrorResponse} format.
 * </p>
 *
 * <p>
 * The exception provides:
 * <ul>
 *   <li>A descriptive error message with the missing task ID</li>
 *   <li>The task ID that caused the exception (for logging and debugging)</li>
 *   <li>Error codes for API responses (HTTP 404 with custom code 40401)</li>
 * </ul>
 * </p>
 *
 * <p>
 * <b>Example error response generated from this exception:</b>
 * <pre>
 * {
 *   "errorCode": 40401,
 *   "errorMessage": "Task with id '550e8400-e29b-41d4-a716-446655440000' not found",
 *   "path": "/api/v1/tasks/550e8400-e29b-41d4-a716-446655440000",
 *   "timestamp": "2026-02-15T12:00:00.123"
 * }
 * </pre>
 * </p>
 *
 * @see com.errorfreetext.exception.GlobalExceptionHandler
 * @see com.errorfreetext.dto.ErrorResponse
 * @see com.errorfreetext.service.TaskService
 */

@Getter
public class TaskNotFoundException extends RuntimeException {

    private final UUID taskId;
    private static final String DEFAULT_MESSAGE = "Task not found";
    private static final String MESSAGE_WITH_ID = "Task with id '%s' not found";

    /**
     * Constructs a new exception with a message containing the missing task ID.
     *
     * @param taskId the UUID of the task that was not found
     */
    public TaskNotFoundException(UUID taskId) {
        super(String.format(MESSAGE_WITH_ID, taskId));
        this.taskId = taskId;
    }

    /**
     * Constructs a new exception with a custom message and the task ID.
     *
     * @param taskId  the UUID of the task that was not found
     * @param message custom error message
     */
    public TaskNotFoundException(UUID taskId, String message) {
        super(message);
        this.taskId = taskId;
    }

    /**
     * Constructs a new exception with a message containing the task ID
     * and the underlying cause.
     *
     * @param taskId the UUID of the task that was not found
     * @param cause  the underlying cause of this exception
     */
    public TaskNotFoundException(UUID taskId, Throwable cause) {
        super(String.format(MESSAGE_WITH_ID, taskId), cause);
        this.taskId = taskId;
    }

    /**
     * Constructs a new exception with a default message.
     * Used when the task ID is not available (e.g., in batch operations).
     */
    public TaskNotFoundException() {
        super(DEFAULT_MESSAGE);
        this.taskId = null;
    }

    /**
     * Returns the error code for the unified error response format.
     * <p>
     * This code is used by the client for programmatic error handling.
     * </p>
     *
     * @return {@code "TASK_NOT_FOUND"} as the error identifier
     */
    public String getErrorCode() {
        return "TASK_NOT_FOUND";
    }

    /**
     * Returns the HTTP status code that should be returned to the client.
     *
     * @return {@code 404} (Not Found)
     */
    public int getHttpStatus() {
        return 404;
    }

    /**
     * Returns the application-specific error code for client handling.
     * <p>
     * Format: {@code 40401} where:
     * <ul>
     *   <li>{@code 404} - HTTP status code</li>
     *   <li>{@code 01} - Specific error type within the 404 category</li>
     * </ul>
     * </p>
     *
     * @return {@code 40401} for task not found errors
     */
    public int getCustomErrorCode() {
        return 40401;
    }
}