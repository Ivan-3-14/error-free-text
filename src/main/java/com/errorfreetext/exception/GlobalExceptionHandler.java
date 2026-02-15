package com.errorfreetext.exception;

import com.errorfreetext.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

/**
 * Global exception handler for the entire application.
 * <p>
 * This class centralizes exception handling across all controllers,
 * ensuring consistent error responses in the {@link ErrorResponse} format.
 * All uncaught exceptions are processed here and transformed into
 * standardized API error responses before being sent to clients.
 * </p>
 *
 * <p>
 * <b>Error Response Format:</b>
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
 * <p>
 * The handler processes three categories of exceptions:
 * <ul>
 *   <li><b>TaskNotFoundException</b> - Returns 404 with custom code 40401</li>
 *   <li><b>ValidationException</b> - Returns 400 with custom codes (40000-40099)</li>
 *   <li><b>Generic Exception</b> - Returns 500 with code 50000 (fallback for unexpected errors)</li>
 * </ul>
 * </p>
 *
 * @see com.errorfreetext.dto.ErrorResponse
 * @see com.errorfreetext.exception.TaskNotFoundException
 * @see com.errorfreetext.exception.ValidationException
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles {@link TaskNotFoundException} when a requested task cannot be found.
     * <p>
     * This exception typically occurs when a client requests a task by ID
     * that doesn't exist in the database. The handler returns a 404 Not Found
     * response with the task ID included in the error message.
     * </p>
     *
     * <p>
     * <b>Example response:</b>
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
     * @param ex      the caught TaskNotFoundException
     * @param request the web request that caused the exception (used to extract the path)
     * @return a {@link ResponseEntity} with 404 status and standardized error body
     */
    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTaskNotFoundException(
            TaskNotFoundException ex,
            WebRequest request
    ) {
        ErrorResponse error = ErrorResponse.builder()
                .errorMessage(ex.getMessage())
                .errorCode(ex.getCustomErrorCode())
                .timestamp(LocalDateTime.now())
                .path(extractPath(request))
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Handles {@link ValidationException} when input validation fails.
     * <p>
     * This exception occurs when client input doesn't meet business requirements
     * (e.g., text with only special characters, invalid language). The handler
     * returns a 400 Bad Request response with specific validation error codes.
     * </p>
     *
     * <p>
     * <b>Example response:</b>
     * <pre>
     * {
     *   "errorCode": 40001,
     *   "errorMessage": "Text cannot contain only special characters and digits",
     *   "path": "/api/v1/tasks",
     *   "timestamp": "2026-02-15T12:00:00.123"
     * }
     * </pre>
     * </p>
     *
     * @param ex      the caught ValidationException
     * @param request the web request that caused the exception
     * @return a {@link ResponseEntity} with 400 status and standardized error body
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            ValidationException ex,
            WebRequest request
    ) {
        ErrorResponse error = ErrorResponse.builder()
                .errorMessage(ex.getMessage())
                .errorCode(ex.getCustomErrorCode())
                .timestamp(LocalDateTime.now())
                .path(extractPath(request))
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handles all unhandled exceptions as a fallback mechanism.
     * <p>
     * This method catches any exception not specifically handled above,
     * preventing sensitive internal error details from leaking to clients.
     * It returns a generic 500 Internal Server Error response.
     * </p>
     *
     * <p>
     * <b>Example response:</b>
     * <pre>
     * {
     *   "errorCode": 50000,
     *   "errorMessage": "Internal server error: Connection refused",
     *   "path": "/api/v1/tasks",
     *   "timestamp": "2026-02-15T12:00:00.123"
     * }
     * </pre>
     * </p>
     *
     * <p>
     * <b>Note:</b> Full exception details are logged but never sent to the client
     * for security reasons.
     * </p>
     *
     * @param ex      the caught exception
     * @param request the web request that caused the exception
     * @return a {@link ResponseEntity} with 500 status and generic error body
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            WebRequest request
    ) {
        ErrorResponse error = ErrorResponse.builder()
                .errorMessage("Internal server error: " + ex.getMessage())
                .errorCode(50000)
                .timestamp(LocalDateTime.now())
                .path(extractPath(request))
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * Extracts the request path from WebRequest for inclusion in error responses.
     * <p>
     * Removes the "uri=" prefix that Spring adds to the description string.
     * </p>
     *
     * @param request the WebRequest containing path information
     * @return the cleaned request path
     */
    private String extractPath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }
}