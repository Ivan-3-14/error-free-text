package com.errorfreetext.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * Container class for the result of a text correction operation.
 * <p>
 * This immutable DTO (Data Transfer Object) encapsulates both the corrected text
 * and the list of Yandex Speller options that were applied during the correction process.
 * It is used to return correction results from the service layer to the scheduler.
 * </p>
 *
 * <p>
 * The class is designed to be immutable (final fields) to ensure thread-safety
 * and prevent accidental modifications after creation. All fields are initialized
 * through the constructor and have no setters.
 * </p>
 *
 * <p>
 * Usage example:
 * <pre>
 * CorrectionResult result = new CorrectionResult(
 *     "Hello world! How are you?",
 *     List.of("IGNORE_DIGITS", "IGNORE_URLS")
 * );
 *
 * String corrected = result.getCorrectedText();
 * List&lt;String&gt; options = result.getOptions();
 * </pre>
 * </p>
 *
 * @see com.errorfreetext.service.TextCorrectionService
 * @see com.errorfreetext.service.impl.TextCorrectionServiceImpl
 */
@Data
@AllArgsConstructor
public class CorrectionResult {

    /**
     * The text after applying all corrections from Yandex Speller API.
     * <p>
     * This field contains the fully corrected version of the original text,
     * with all detected errors replaced by their suggested corrections.
     * If no corrections were needed or applied, this will be identical to
     * the original text.
     * </p>
     */
    private final String correctedText;

    /**
     * The list of Yandex Speller options that were determined and applied
     * during the correction process.
     * <p>
     * Options are determined based on the content of the original text:
     * <ul>
     *   <li>{@code IGNORE_DIGITS} - added if text contains any digits</li>
     *   <li>{@code IGNORE_URLS} - added if text contains any URLs</li>
     * </ul>
     * The list may be empty if neither condition was met.
     * </p>
     *
     * <p>
     * These options are stored with the task in the database for auditing
     * and debugging purposes, allowing later analysis of why certain
     * corrections were or weren't applied.
     * </p>
     *
     * @see com.errorfreetext.entity.Task#getOptions()
     */
    private final List<String> options;
}