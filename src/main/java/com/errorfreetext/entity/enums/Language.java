package com.errorfreetext.entity.enums;

/**
 * Supported languages for text correction.
 * <p>
 * Used to specify the language of the input text when calling Yandex Speller API
 * and for validation of incoming requests.
 * </p>
 *
 * <p>
 * The language affects:
 * <ul>
 *   <li>Dictionary used for spell checking (Russian or English words)</li>
 *   <li>Grammar rules applied during correction</li>
 *   <li>Character set validation</li>
 * </ul>
 * </p>
 */
public enum Language {

    /** Russian language. Uses Russian dictionary and grammar rules. */
    RU,

    /** English language. Uses English dictionary and grammar rules. */
    EN
}