package com.errorfreetext.service.impl;

import com.errorfreetext.exception.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
@DisplayName("ValidationServiceImpl Unit Tests")
class ValidationServiceImplTest {

    private final ValidationServiceImpl validationService = new ValidationServiceImpl();

    @Nested
    @DisplayName("validateTextContent() method tests")
    class ValidateTextContentTests {

        @Test
        @DisplayName("Should pass validation for normal text with letters")
        void shouldPassValidationForNormalTextWithLetters() {
            String validText = "Hello world! How are you?";

            assertThatCode(() -> validationService.validateTextContent(validText))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should pass validation for text with letters and digits")
        void shouldPassValidationForTextWithLettersAndDigits() {
            String validText = "Hello123 world456";

            assertThatCode(() -> validationService.validateTextContent(validText))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should pass validation for text with letters and punctuation")
        void shouldPassValidationForTextWithLettersAndPunctuation() {
            String validText = "Hello, world! How are you?";

            assertThatCode(() -> validationService.validateTextContent(validText))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should pass validation for text with letters, digits, and punctuation")
        void shouldPassValidationForTextWithLettersDigitsAndPunctuation() {
            String validText = "Hello123, world456! How are you?";

            assertThatCode(() -> validationService.validateTextContent(validText))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should pass validation for text with Unicode letters (non-ASCII)")
        void shouldPassValidationForTextWithUnicodeLetters() {
            String validText = "Привет мир! こんにちは 世界";

            assertThatCode(() -> validationService.validateTextContent(validText))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should throw ValidationException for text with only emojis (considered special chars)")
        void shouldThrowValidationExceptionForTextWithOnlyEmojis() {
            String emojiText = "😀😃😄😁😆";

            assertThatThrownBy(() -> validationService.validateTextContent(emojiText))
                    .isInstanceOf(ValidationException.class)
                    .hasMessage("Text cannot contain only special characters and digits");
        }

        @Test
        @DisplayName("Should pass validation for text with emojis and letters")
        void shouldPassValidationForTextWithEmojisAndLetters() {
            String mixedText = "Hello 😀 world!";

            assertThatCode(() -> validationService.validateTextContent(mixedText))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should pass validation for single letter")
        void shouldPassValidationForSingleLetter() {
            String singleLetter = "a";

            assertThatCode(() -> validationService.validateTextContent(singleLetter))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should throw ValidationException for single digit")
        void shouldThrowValidationExceptionForSingleDigit() {
            String singleDigit = "5";

            assertThatThrownBy(() -> validationService.validateTextContent(singleDigit))
                    .isInstanceOf(ValidationException.class);
        }

        @Test
        @DisplayName("Should throw ValidationException for single special character")
        void shouldThrowValidationExceptionForSingleSpecialCharacter() {
            String singleSpecial = "@";

            assertThatThrownBy(() -> validationService.validateTextContent(singleSpecial))
                    .isInstanceOf(ValidationException.class);
        }

        @Test
        @DisplayName("Should throw ValidationException for text with only spaces")
        void shouldThrowValidationExceptionForTextWithOnlySpaces() {
            String spaces = "     ";

            assertThatThrownBy(() -> validationService.validateTextContent(spaces))
                    .isInstanceOf(ValidationException.class);
        }

        @Test
        @DisplayName("Should throw ValidationException for text with only newlines")
        void shouldThrowValidationExceptionForTextWithOnlyNewlines() {
            String newlines = "\n\n\n";

            assertThatThrownBy(() -> validationService.validateTextContent(newlines))
                    .isInstanceOf(ValidationException.class);
        }

        @Test
        @DisplayName("Should throw ValidationException for text with only tabs")
        void shouldThrowValidationExceptionForTextWithOnlyTabs() {
            String tabs = "\t\t\t";

            assertThatThrownBy(() -> validationService.validateTextContent(tabs))
                    .isInstanceOf(ValidationException.class);
        }

        @Test
        @DisplayName("Should throw ValidationException for mixed whitespace characters")
        void shouldThrowValidationExceptionForMixedWhitespace() {
            String mixedWhitespace = " \t\n\r ";

            assertThatThrownBy(() -> validationService.validateTextContent(mixedWhitespace))
                    .isInstanceOf(ValidationException.class);
        }

        @Test
        @DisplayName("Should handle null text gracefully (though controller should prevent it)")
        void shouldHandleNullText() {

            assertThatThrownBy(() -> validationService.validateTextContent(null))
                    .isInstanceOf(ValidationException.class);
        }
    }
}