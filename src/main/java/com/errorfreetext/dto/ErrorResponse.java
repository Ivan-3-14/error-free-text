package com.errorfreetext.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Standard error response DTO for consistent error handling across all REST endpoints.
 * <p>
 * This class defines the uniform error format that the API returns when any exception
 * occurs. All controllers should return errors in this format to provide a consistent
 * experience for API clients.
 * </p> *
 * <p>
 * <b>Example error response:</b>
 * <pre>
 * {
 *   "errorCode": 40401,
 *   "errorMessage": "Task with id: 550e8400-e29b-41d4-a716-446655440000 not found",
 *   "path": "/api/v1/tasks/550e8400-e29b-41d4-a716-446655440000",
 *   "timestamp": "2026-02-15T12:00:00.123"
 * }
 * </pre>
 * </p>
 *
 * <p>
 * Error code conventions:
 * <ul>
 *   <li><b>40xxx</b> - Client errors (4xx HTTP status)</li>
 *   <li><b>40401</b> - Task not found</li>
 *   <li><b>40001</b> - Validation error</li>
 *   <li><b>500xx</b> - Server errors (5xx HTTP status)</li>
 * </ul>
 * </p>
 *
 * @see com.errorfreetext.exception.GlobalExceptionHandler
 * @see com.errorfreetext.exception.TaskNotFoundException
 * @see com.errorfreetext.exception.ValidationException
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    /**
     * Application-specific error code for programmatic handling.
     * <p>
     * This code provides more granular error information than HTTP status codes alone.
     * Clients can use this code to implement specific error handling logic without
     * parsing error messages.
     * </p>
     * <p>
     * Examples: {@code 40401} (Task not found), {@code 40001} (Validation error)
     * </p>
     */
    private Integer errorCode;

    /**
     * Human-readable error message describing what went wrong.
     * <p>
     * This message is intended for developers and should not be displayed directly
     * to end users without additional processing. It provides context about the error
     * and may include relevant identifiers (like task IDs).
     * </p>
     * <p>
     * Example: {@code "Task with id: 550e8400-e29b-41d4-a716-446655440000 not found"}
     * </p>
     */
    private String errorMessage;

    /**
     * The request path that caused the error.
     * <p>
     * Useful for debugging and monitoring to identify which endpoint
     * generated the error. This is particularly helpful in microservice
     * architectures where requests may pass through multiple services.
     * </p>
     * <p>
     * Example: {@code "/api/v1/tasks/550e8400-e29b-41d4-a716-446655440000"}
     * </p>
     */
    private String path;

    /**
     * Timestamp when the error occurred, in ISO 8601 format with milliseconds.
     * <p>
     * Format: {@code yyyy-MM-dd'T'HH:mm:ss.SSS}
     * </p>
     * <p>
     * Example: {@code "2026-02-15T12:00:00.123"}
     * </p>
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime timestamp;
}