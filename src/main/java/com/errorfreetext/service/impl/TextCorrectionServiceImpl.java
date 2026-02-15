package com.errorfreetext.service.impl;

import com.errorfreetext.dto.CorrectionResult;
import com.errorfreetext.entity.Task;
import com.errorfreetext.entity.enums.TaskStatus;
import com.errorfreetext.service.TextCorrectionService;
import com.errorfreetext.service.YandexSpellerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Service implementation for text correction operations.
 * Handles the core business logic of correcting text using Yandex Speller API,
 * including text chunking, option detection, and error handling.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TextCorrectionServiceImpl implements TextCorrectionService {

    /**
     * Maximum allowed size for a single chunk when splitting large texts.
     * Based on Yandex Speller API limit of 10,000 characters per request.
     */
    private static final int MAX_CHUNK_SIZE = 10000;

    /**
     * Regular expression pattern for detecting digits in text
     */
    private static final Pattern DIGIT_PATTERN = Pattern.compile("\\d");

    /**
     * Regular expression pattern for detecting URLs in text.
     * Matches HTTP/HTTPS URLs with various domain formats and query parameters.
     */
    private static final Pattern URL_PATTERN = Pattern.compile(
            "https?://(www\\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_+.~#?&/=]*)",
            Pattern.CASE_INSENSITIVE
    );

    private final YandexSpellerService yandexSpellerService;

    /**
     * Performs text correction on a given task.
     * The process includes:
     * <ul>
     *   <li>Analyzing the text to determine required Speller options</li>
     *   <li>Splitting large texts into chunks within API limits</li>
     *   <li>Sending each chunk to Yandex Speller API for correction</li>
     *   <li>Reassembling corrected chunks into the final text</li>
     * </ul>
     *
     * @param task the task containing the original text to be corrected
     * @return a {@link CorrectionResult} containing the corrected text and applied options
     * @throws RuntimeException if any error occurs during the correction process
     */
    @Transactional
    @Override
    public CorrectionResult correctText(Task task) {
        log.info("Starting text correction for task: {}", task.getId());
        task.setStatus(TaskStatus.PROCESSING);

        try {
            List<String> options = determineOptions(task.getOriginalText());
            task.setOptions(options);

            List<String> chunks = splitTextIntoChunks(task.getOriginalText());
            List<String> correctedChunks = new ArrayList<>();

            for (String chunk : chunks) {
                String correctedChunk = yandexSpellerService.correctText(chunk, task.getLanguage().name(), options);
                correctedChunks.add(correctedChunk);
            }

            String correctedText = String.join("", correctedChunks);
            task.setCorrectedText(correctedText);
            task.setStatus(TaskStatus.COMPLETED);

            log.info("Text correction completed for task: {}", task.getId());
            return new CorrectionResult(correctedText, options);
        } catch (Exception e) {
            log.error("Error correcting text for task: {}", task.getId(), e);
            task.setStatus(TaskStatus.FAILED);
            task.setErrorMessage("Correction failed: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Determines which Yandex Speller options should be enabled based on text content.
     * According to requirements:
     * <ul>
     *   <li>IGNORE_DIGITS - enabled if text contains any digits</li>
     *   <li>IGNORE_URLS - enabled if text contains any URLs</li>
     *   <li>FIND_REPEAT_WORDS and IGNORE_CAPITALIZATION are always disabled</li>
     * </ul>
     *
     * @param text the text to analyze for option determination
     * @return a list of option names to be enabled for this text
     */
    private List<String> determineOptions(String text) {
        List<String> options = new ArrayList<>();

        if (containsDigits(text)) {
            options.add("IGNORE_DIGITS");
        }

        if (containsUrls(text)) {
            options.add("IGNORE_URLS");
        }
        return options;
    }

    /**
     * Checks if the given text contains any digit characters.
     *
     * @param text the text to check
     * @return true if at least one digit is found, false otherwise
     */
    private boolean containsDigits(String text) {
        return DIGIT_PATTERN.matcher(text).find();
    }

    /**
     * Checks if the given text contains any URLs matching the URL pattern.
     *
     * @param text the text to check
     * @return true if at least one URL is found, false otherwise
     */
    private boolean containsUrls(String text) {
        return URL_PATTERN.matcher(text).find();
    }

    /**
     * Splits a text into chunks that do not exceed the maximum allowed size for Yandex Speller API.
     * The splitting algorithm attempts to break at word boundaries (spaces) to avoid cutting words.
     * If no space is found within the chunk, it will break at the maximum length.
     *
     * @param text the text to split into chunks
     * @return a list of text chunks, each not exceeding MAX_CHUNK_SIZE
     * @throws IllegalArgumentException if MAX_CHUNK_SIZE is not positive
     */
    private List<String> splitTextIntoChunks(String text) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return chunks;
        }
        if (MAX_CHUNK_SIZE <= 0) {
            throw new IllegalArgumentException("MAX_CHUNK_SIZE must be > 0");
        }

        int length = text.length();
        int i = 0;
        while (i < length) {
            int end = Math.min(length, i + MAX_CHUNK_SIZE);

            if (end < length) {
                int lastSpace = text.lastIndexOf(' ', end - 1);
                if (lastSpace > i) {
                    end = lastSpace;
                } else {
                    end = Math.min(length, i + MAX_CHUNK_SIZE);
                }
            }
            if (end == i) {
                end = Math.min(length, i + 1);
            }
            chunks.add(text.substring(i, end));
            i = end;
            while (i < length && Character.isWhitespace(text.charAt(i))) {
                i++;
            }
        }
        return chunks;
    }
}