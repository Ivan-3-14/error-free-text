package com.errorfreetext.service;

import com.errorfreetext.dto.CorrectionResult;
import com.errorfreetext.entity.Task;

/**
 * Service interface for performing text correction operations.
 * <p>
 * Defines the contract for correcting text using external spell checking services.
 * The main responsibility is to process a task containing original text and return
 * the corrected version along with applied options.
 * </p>
 *
 * <p>
 * The correction process typically involves:
 * <ul>
 *   <li>Analyzing text to determine required spell checker options</li>
 *   <li>Splitting large texts into chunks within API limits</li>
 *   <li>Communicating with external spell checking service (Yandex Speller)</li>
 *   <li>Reassembling corrected chunks into final text</li>
 * </ul>
 * </p>
 *
 * @see com.errorfreetext.entity.Task
 * @see com.errorfreetext.dto.CorrectionResult
 * @see com.errorfreetext.service.impl.TextCorrectionServiceImpl
 */
public interface TextCorrectionService {

    /**
     * Corrects the text contained in the given task.
     * <p>
     * Processes the task's original text through the spell checking service
     * and returns the corrected version. The method also determines which
     * spell checker options (IGNORE_DIGITS, IGNORE_URLS, etc.) should be
     * applied based on the text content.
     * </p>
     *
     * <p>
     * The task object is updated during processing:
     * <ul>
     *   <li>Status is updated to PROCESSING at start</li>
     *   <li>Options are set based on text analysis</li>
     *   <li>Corrected text is set upon successful completion</li>
     *   <li>Status is updated to COMPLETED when done</li>
     * </ul>
     * In case of errors, the exception is propagated to the caller
     * for proper error handling.
     * </p>
     *
     * @param task the task containing the original text to be corrected
     * @return a {@link CorrectionResult} containing the corrected text and
     *         the list of options that were applied during correction
     * @throws RuntimeException if any error occurs during the correction process
     *         (API errors, network issues, parsing errors, etc.)
     *
     * @see com.errorfreetext.entity.enums.TaskStatus
     * @see com.errorfreetext.dto.CorrectionResult
     */
    CorrectionResult correctText(Task task);
}