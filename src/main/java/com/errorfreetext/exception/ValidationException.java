package com.errorfreetext.exception;

import com.errorfreetext.dto.ErrorResponse;
import lombok.Getter;

/**
 * Exception thrown when input validation fails.
 * <p>
 * This exception is used throughout the service layer to indicate that
 * user input does not meet the required business rules or format constraints.
 * It integrates with the global exception handler to produce consistent
 * error responses in the {@link ErrorResponse} format.
 * </p>
 *
 * <p>
 * The exception provides detailed information about the validation failure:
 * <ul>
 *   <li>Descriptive error message explaining what failed</li>
 *   <li>The specific field that caused the validation error (if applicable)</li>
 *   <li>Error codes for API responses (HTTP 400 with custom codes)</li>
 * </ul>
 * </p>
 *
 * <p>
 * <b>Example error response generated from this exception:</b>
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
 * @see com.errorfreetext.exception.GlobalExceptionHandler
 * @see com.errorfreetext.dto.ErrorResponse
 * @see com.errorfreetext.service.ValidationService
 */
@Getter
public class ValidationException extends RuntimeException {

    private final String field;
    private final String errorCode;
    private final int customErrorCode;

    /**
     * Constructs a new validation exception with only a message.
     * <p>
     * Uses default error codes: {@code errorCode="VALIDATION_ERROR"}, {@code customErrorCode=40000}.
     * </p>
     *
     * @param message the detail message explaining the validation failure
     */
    public ValidationException(String message) {
        super(message);
        this.field = null;
        this.errorCode = "VALIDATION_ERROR";
        this.customErrorCode = 40000;
    }

    /**
     * Constructs a new validation exception with a message and the invalid field name.
     * <p>
     * Uses default error codes: {@code errorCode="VALIDATION_ERROR"}, {@code customErrorCode=40000}.
     * </p>
     *
     * @param message the detail message explaining the validation failure
     * @param field   the name of the field that failed validation
     */
    public ValidationException(String message, String field) {
        super(message);
        this.field = field;
        this.errorCode = "VALIDATION_ERROR";
        this.customErrorCode = 40000;
    }

    /**
     * Constructs a new validation exception with a message and custom error codes.
     *
     * @param message         the detail message explaining the validation failure
     * @param errorCode       the error code for programmatic handling
     * @param customErrorCode the application-specific error code for client handling
     */
    public ValidationException(String message, String errorCode, int customErrorCode) {
        super(message);
        this.field = null;
        this.errorCode = errorCode;
        this.customErrorCode = customErrorCode;
    }

    /**
     * Constructs a new validation exception with full details including the invalid field.
     *
     * @param message         the detail message explaining the validation failure
     * @param field           the name of the field that failed validation
     * @param errorCode       the error code for programmatic handling
     * @param customErrorCode the application-specific error code for client handling
     */
    public ValidationException(String message, String field, String errorCode, int customErrorCode) {
        super(message);
        this.field = field;
        this.errorCode = errorCode;
        this.customErrorCode = customErrorCode;
    }

    /**
     * Returns the HTTP status code that should be returned to the client.
     *
     * @return {@code 400} (Bad Request)
     */
    public int getHttpStatus() {
        return 400;
    }
}