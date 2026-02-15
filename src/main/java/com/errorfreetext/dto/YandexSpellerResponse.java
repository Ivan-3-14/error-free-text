package com.errorfreetext.dto;

import lombok.Data;

import java.util.List;

@Data
public class YandexSpellerResponse {

    /**
     * The misspelled word as it appears in the original text.
     * <p>
     * This is the exact substring that was detected as potentially incorrect.
     * The word includes any punctuation attached to it (e.g., "Hello," would be "Hello")
     * but the position field accounts for the actual text location.
     * </p>
     * <p>
     * Example: {@code "Helo"}
     * </p>
     */
    private String word;

    /**
     * List of suggested corrections for the misspelled word.
     * <p>
     * Suggestions are ordered by relevance, with the most likely correction
     * as the first element. The service typically uses the first suggestion
     * (index 0) for automatic correction.
     * </p>
     * <p>
     * Example: {@code ["Hello", "Hero", "Help"]}
     * </p>
     */
    private List<String> s;


    /**
     * Zero-based character position where the misspelled word starts
     * in the original text.
     * <p>
     * This position is absolute within the whole text (not line or word number).
     * Used to locate and replace the word in the original string.
     * </p>
     * <p>
     * Example: For text "Hello world", the word "world" would have position 6
     * </p>
     */
    private int pos;

    /**
     * Length of the misspelled word in characters.
     * <p>
     * The original word from position {@code pos} to {@code pos + len}
     * should exactly match the {@code word} field. This length is used
     * to correctly replace the word even if the correction has different length.
     * </p>
     * <p>
     * Example: For word "Helo" (length 4)
     * </p>
     */
    private int len;
}