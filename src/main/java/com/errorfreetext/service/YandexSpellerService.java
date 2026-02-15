package com.errorfreetext.service;

import java.util.List;

/**
 * Service interface for communicating with Yandex Speller API.
 * <p>
 * Defines the contract for text correction using the external Yandex Speller service.
 * This interface abstracts the HTTP communication, request formatting, response parsing,
 * and error handling required to interact with the Yandex Speller API.
 * </p>
 *
 * <p>
 * The Yandex Speller API provides spell checking and correction for multiple languages
 * with various configurable options. This service handles:
 * <ul>
 *   <li>Building properly formatted requests with text, language, and options</li>
 *   <li>Sending HTTP POST requests to the appropriate endpoint</li>
 *   <li>Parsing JSON responses containing word corrections</li>
 *   <li>Applying corrections to the original text while preserving structure</li>
 *   <li>Handling API errors and timeouts gracefully</li>
 * </ul>
 * </p>
 *
 * <p>
 * The API expects form-urlencoded parameters and returns a nested array structure
 * where each correction contains the original word, suggested replacements, and
 * position information. This service handles the complexity of this response format
 * and provides a simple corrected string to callers.
 * </p>
 *
 * @see <a href="https://yandex.ru/dev/speller/">Yandex Speller API Documentation</a>
 * @see com.errorfreetext.dto.YandexSpellerResponse
 * @see com.errorfreetext.service.impl.YandexSpellerServiceImpl
 */
public interface YandexSpellerService {

    /**
     * Corrects a single text chunk using Yandex Speller API.
     * <p>
     * Sends the provided text to Yandex Speller for correction according to the
     * specified language and options. The method handles all aspects of API
     * communication including:
     * </p>
     *
     * <ul>
     *   <li>Converting options list to API bitmask format</li>
     *   <li>Building form-urlencoded request with proper parameters</li>
     *   <li>Setting appropriate HTTP headers</li>
     *   <li>Parsing the nested array response structure</li>
     *   <li>Applying corrections in reverse order (from end to beginning)</li>
     *   <li>Validating word positions before applying changes</li>
     * </ul>
     *
     * <p>
     * Important implementation notes:
     * <ul>
     *   <li>The API returns a {@code List<List<YandexSpellerResponse>>} where the first
     *       inner list contains corrections for the provided text</li>
     *   <li>Corrections are applied from the end of the text to the beginning to prevent
     *       position shifts from affecting subsequent corrections</li>
     *   <li>Each correction is validated by checking that the word at the specified
     *       position actually matches the reported wrong word</li>
     *   <li>If no corrections are returned, the original text is returned unchanged</li>
     * </ul>
     * </p>
     *
     * <p>
     * Error handling:
     * <ul>
     *   <li>Network errors or API unavailability result in {@link RuntimeException}</li>
     *   <li>Malformed responses are logged and re-thrown as runtime exceptions</li>
     *   <li>Word mismatches (when expected word doesn't match actual text) are logged
     *       as warnings but don't prevent other corrections from being applied</li>
     * </ul>
     * </p>
     *
     * @param text    the text chunk to be corrected (should be within API size limits,
     *                typically ≤ 10,000 characters)
     * @param lang    the language code for spell checking (e.g., "en", "ru")
     * @param options list of Yandex Speller options to enable
     *                (supported: "IGNORE_DIGITS", "IGNORE_URLS")
     * @return the corrected text with all valid replacements applied,
     *         or the original text if no corrections were made
     * @throws RuntimeException if the API call fails, returns an invalid response,
     *         or any other error occurs during processing
     *
     * @see com.errorfreetext.dto.YandexSpellerResponse
     */
    String correctText(String text, String lang, List<String> options);
}