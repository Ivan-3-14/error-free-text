package com.errorfreetext.service.impl;

import com.errorfreetext.config.YandexSpellerConfig;
import com.errorfreetext.service.YandexSpellerService;
import com.errorfreetext.dto.YandexSpellerResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

/**
 * Service implementation for communicating with Yandex Speller API.
 * Handles text correction requests, response parsing, and applying corrections to the original text.
 *
 * @see <a href="https://yandex.ru/dev/speller/">Yandex Speller API Documentation</a>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class YandexSpellerServiceImpl implements YandexSpellerService {

    /**
     * REST client for making HTTP requests to external APIs.
     */
    private final RestTemplate restTemplate;

    /**
     * Configuration properties for Yandex Speller API.
     * Contains API URL, timeouts, chunk sizes, and retry settings.
     * Automatically populated from application properties with prefix "yandex.speller".
     */
    private final YandexSpellerConfig spellerConfig;

    /**
     * Corrects the given text using Yandex Speller API.
     * The process includes:
     * <ul>
     *   <li>Building a form-urlencoded request with text, language, and options</li>
     *   <li>Sending a POST request to Yandex Speller API</li>
     *   <li>Parsing the response which returns corrections as a list of lists</li>
     *   <li>Applying corrections to the original text from end to beginning</li>
     * </ul>
     *
     * @param text    the text to be corrected
     * @param lang    the language code (e.g., "en", "ru")
     * @param options list of Yandex Speller options to apply (IGNORE_DIGITS, IGNORE_URLS, etc.)
     * @return the corrected text
     * @throws RuntimeException if API call fails or response cannot be processed
     */
    @Override
    public String correctText(String text, String lang, List<String> options) {
        try {
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("text", text);
            params.add("lang", lang.toLowerCase());
            params.add("options", String.valueOf(optionsToBitmask(options)));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);

            ParameterizedTypeReference<List<List<YandexSpellerResponse>>> typeRef =
                    new ParameterizedTypeReference<>() {
                    };

            ResponseEntity<List<List<YandexSpellerResponse>>> responseEntity =
                    restTemplate.exchange(spellerConfig.getApiUrl(), HttpMethod.POST, requestEntity, typeRef);

            List<List<YandexSpellerResponse>> responsesList = responseEntity.getBody();

            if (responsesList != null && !responsesList.isEmpty() && !responsesList.get(0).isEmpty()) {
                List<YandexSpellerResponse> corrections = responsesList.get(0);
                return applyCorrections(text, corrections.toArray(new YandexSpellerResponse[0]));
            }
            return text;
        } catch (Exception e) {
            log.error("Error calling Yandex Speller API via POST", e);
            throw new RuntimeException("Failed to correct text: " + e.getMessage(), e);
        }
    }

    /**
     * Applies corrections to the original text based on Yandex Speller API response.
     * Corrections are applied from the end of the text to the beginning to prevent
     * position shifts from affecting subsequent corrections.
     *
     * @param originalText the original text before correction
     * @param responses    array of corrections from Yandex API
     * @return the text with all corrections applied
     */
    private String applyCorrections(String originalText, YandexSpellerResponse[] responses) {
        if (responses == null || responses.length == 0) {
            return originalText;
        }

        StringBuilder result = new StringBuilder(originalText);
        Arrays.sort(responses, (a, b) -> Integer.compare(b.getPos(), a.getPos()));

        for (YandexSpellerResponse correction : responses) {
            if (correction != null && correction.getS() != null && !correction.getS().isEmpty()) {
                String wrongWord = correction.getWord();
                String correctWord = correction.getS().get(0);
                int position = correction.getPos();
                int length = correction.getLen();

                if (position >= 0 && position + length <= result.length()) {
                    String actualWord = result.substring(position, position + length);
                    if (actualWord.equals(wrongWord)) {
                        log.debug("Replacing '{}' with '{}' at position {}",
                                wrongWord, correctWord, position);
                        result.replace(position, position + length, correctWord);
                    } else {
                        log.warn("Word mismatch at position {}: expected '{}' but found '{}'",
                                position, wrongWord, actualWord);
                    }
                }
            }
        }

        String finalText = result.toString();
        log.info("Corrected text: '{}'", finalText);
        return finalText;
    }

    /**
     * Converts a list of Yandex Speller option names to a bitmask integer.
     * Bitmask values:
     * <ul>
     *   <li>2 (bit 1) - IGNORE_DIGITS: ignore words containing digits</li>
     *   <li>4 (bit 2) - IGNORE_URLS: ignore URLs and email addresses</li>
     * </ul>
     * Note: Options can be combined by adding their values (e.g., 2 + 4 = 6 for both options).
     *
     * @param options list of option names
     * @return bitmask integer for Yandex Speller API
     */
    private int optionsToBitmask(List<String> options) {
        int bitmask = 0;
        if (options != null) {
            if (options.contains("IGNORE_DIGITS")) bitmask |= 2;
            if (options.contains("IGNORE_URLS")) bitmask |= 4;
        }
        return bitmask;
    }
}