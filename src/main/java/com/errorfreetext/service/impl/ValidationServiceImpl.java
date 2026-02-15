package com.errorfreetext.service.impl;

import com.errorfreetext.exception.ValidationException;
import com.errorfreetext.service.ValidationService;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

/**
 * Service implementation for validating text content before task creation.
 * Ensures that input text meets the business requirements defined in the technical specification.
 */
@Service
public class ValidationServiceImpl implements ValidationService {

    /**
     * Pattern to check if text contains at least one Unicode letter.
     * \p{L} matches any kind of letter from any language.
     */
    private static final Pattern HAS_LETTER = Pattern.compile("\\p{L}");

    /**
     * Pattern to check if text consists ONLY of special characters.
     * Used for the specific error message from requirements.
     */
    private static final Pattern SPECIAL_CHARS_ONLY = Pattern.compile("^[\\s\\d\\p{Punct}\\p{So}]+$");

    @Override
    public void validateTextContent(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new ValidationException(
                    "Text cannot be empty",
                    "VALIDATION_ERROR",
                    40001
            );
        }

        if (!HAS_LETTER.matcher(text).find()) {
            if (SPECIAL_CHARS_ONLY.matcher(text).matches()) {
                throw new ValidationException(
                        "Text cannot contain only special characters and digits",
                        "VALIDATION_ERROR",
                        40001
                );
            }

            throw new ValidationException(
                    "Text must contain at least one letter",
                    "VALIDATION_ERROR",
                    40001
            );
        }
    }
}