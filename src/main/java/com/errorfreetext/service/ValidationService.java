package com.errorfreetext.service;

import com.errorfreetext.exception.ValidationException;

/**
 * Service interface for validating text content before task creation.
 * <p>
 * Defines the contract for input validation according to the technical specification.
 * Ensures that incoming text meets the business requirements before being
 * persisted as a correction task.
 * </p>
 *
 * <p>
 * The validation rules enforced by this service:
 * <ul>
 *   <li>Text length is already validated at controller level (minimum 3 characters via @Size)</li>
 *   <li>Text must contain at least one meaningful word character</li>
 *   <li>Text cannot consist exclusively of whitespace, digits, and punctuation</li>
 * </ul>
 * </p>
 *
 * <p>
 * This validation is complementary to the controller-level validation and
 * focuses on semantic content rather than basic formatting.
 * </p>
 *
 * @see com.errorfreetext.dto.CreateTaskRequest
 * @see com.errorfreetext.exception.ValidationException
 * @see com.errorfreetext.service.impl.ValidationServiceImpl
 */
public interface ValidationService {

    /**
     * Validates the content of a text according to business rules.
     * <p>
     * Performs semantic validation to ensure the text contains actual words
     * and is not just a sequence of special characters, digits, or whitespace.
     * </p>
     *
     * <p>
     * Validation checks:
     * <ul>
     *   <li>Text must contain at least one letter or meaningful word character</li>
     *   <li>Text cannot consist solely of: whitespace, digits, punctuation marks</li>
     * </ul>
     * </p>
     *
     * <p>
     * This method does NOT check:
     * <ul>
     *   <li>Text length (handled by @Size in controller)</li>
     *   <li>Language parameter (validated separately)</li>
     *   <li>Spelling or grammar (handled by correction service)</li>
     * </ul>
     * </p>
     *
     * @param text the text content to validate
     * @throws ValidationException if the text contains only special characters and digits
     *         with error code "VALIDATION_ERROR" and HTTP status 400
     *
     * @see com.errorfreetext.exception.ValidationException
     * @see com.errorfreetext.dto.ErrorResponse
     */
    void validateTextContent(String text);
}