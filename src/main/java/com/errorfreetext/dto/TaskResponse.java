package com.errorfreetext.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for text correction task information.
 * <p>
 * This class represents the data returned to clients when they create a task
 * or request task status. Fields are conditionally included based on the
 * task's current state to provide only relevant information.
 * </p>
 *
 * <p>
 * <b>Example responses by status:</b>
 *
 * <p><b>PENDING (just created):</b>
 * <pre>
 * {
 *   "id": "550e8400-e29b-41d4-a716-446655440000",
 *   "status": "PENDING",
 *   "createdAt": "2026-02-15T12:00:00.123",
 *   "updatedAt": "2026-02-15T12:00:00.123"
 * }
 * </pre>
 *
 * <p><b>PROCESSING:</b>
 * <pre>
 * {
 *   "id": "550e8400-e29b-41d4-a716-446655440000",
 *   "status": "PROCESSING",
 *   "createdAt": "2026-02-15T12:00:00.123",
 *   "updatedAt": "2026-02-15T12:00:05.456"
 * }
 * </pre>
 *
 * <p><b>COMPLETED:</b>
 * <pre>
 * {
 *   "id": "550e8400-e29b-41d4-a716-446655440000",
 *   "status": "COMPLETED",
 *   "correctedText": "Hello world! How are you?",
 *   "createdAt": "2026-02-15T12:00:00.123",
 *   "updatedAt": "2026-02-15T12:00:10.789"
 * }
 * </pre>
 *
 * <p><b>FAILED:</b>
 * <pre>
 * {
 *   "id": "550e8400-e29b-41d4-a716-446655440000",
 *   "status": "FAILED",
 *   "errorMessage": "Yandex Speller API timeout",
 *   "createdAt": "2026-02-15T12:00:00.123",
 *   "updatedAt": "2026-02-15T12:00:10.789"
 * }
 * </pre>
 * </p>
 *
 * @see com.errorfreetext.entity.Task
 * @see com.errorfreetext.entity.enums.TaskStatus
 * @see com.errorfreetext.controller.TaskController
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskResponse {

    /**
     * Unique identifier of the task.
     * <p>
     * Generated using UUID v4 strategy. This ID should be used by clients
     * to retrieve the task status and corrected text later.
     * </p>
     * <p>
     * Example: {@code "550e8400-e29b-41d4-a716-446655440000"}
     * </p>
     */
    private UUID id;

    /**
     * Current status of the task in its lifecycle.
     * <p>
     * Possible values:
     * <ul>
     *   <li><b>PENDING</b> - Task created, waiting for processing</li>
     *   <li><b>PROCESSING</b> - Task is currently being corrected</li>
     *   <li><b>COMPLETED</b> - Text correction finished successfully</li>
     *   <li><b>FAILED</b> - Error occurred during correction</li>
     * </ul>
     * </p>
     * <p>
     * Example: {@code "COMPLETED"}
     * </p>
     */
    private String status;

    /**
     * The corrected version of the original text.
     * <p>
     * This field is only present when the task status is {@code COMPLETED}.
     * Contains the fully corrected text with all detected errors fixed.
     * </p>
     * <p>
     * Example: {@code "Hello world! How are you?"}
     * </p>
     */
    private String correctedText;

    /**
     * Error message describing why the task failed.
     * <p>
     * This field is only present when the task status is {@code FAILED}.
     * Provides details about the error for debugging purposes.
     * </p>
     * <p>
     * Example: {@code "Yandex Speller API timeout after 10 seconds"}
     * </p>
     */
    private String errorMessage;

    /**
     * Timestamp when the task was created.
     * <p>
     * Set automatically when the task is first persisted. This field never changes
     * throughout the task's lifecycle.
     * </p>
     * <p>
     * Format: {@code yyyy-MM-dd'T'HH:mm:ss.SSS}
     * </p>
     * <p>
     * Example: {@code "2026-02-15T12:00:00.123"}
     * </p>
     */
    private LocalDateTime createdAt;

    /**
     * Timestamp when the task was last updated.
     * <p>
     * Updated automatically whenever the task status changes or when
     * the corrected text is set. This field helps track task progress
     * and detect stuck tasks.
     * </p>
     * <p>
     * Format: {@code yyyy-MM-dd'T'HH:mm:ss.SSS}
     * </p>
     * <p>
     * Example: {@code "2026-02-15T12:00:10.789"}
     * </p>
     */
    private LocalDateTime updatedAt;
}