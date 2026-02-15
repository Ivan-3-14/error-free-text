package com.errorfreetext.service.impl;

import com.errorfreetext.dto.CorrectionResult;
import com.errorfreetext.entity.Task;
import com.errorfreetext.entity.enums.Language;
import com.errorfreetext.entity.enums.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.errorfreetext.service.YandexSpellerService;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TextCorrectionServiceImpl Unit Tests")
class TextCorrectionServiceImplTest {

    @Mock
    private YandexSpellerService yandexSpellerService;

    @InjectMocks
    private TextCorrectionServiceImpl textCorrectionService;

    private Task task;
    private String originalText;

    @BeforeEach
    void setUp() {
        UUID taskId = UUID.randomUUID();
        originalText = "Hello world! How are you?";

        task = Task.builder()
                .id(taskId)
                .originalText(originalText)
                .language(Language.EN)
                .status(TaskStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("correctText() method tests")
    class CorrectTextTests {

        @Test
        @DisplayName("Should successfully correct text when task is valid")
        void shouldSuccessfullyCorrectTextWhenTaskIsValid() {
            String correctedChunk = "Hello world! How are you?";
            when(yandexSpellerService.correctText(eq(originalText), eq("EN"), anyList()))
                    .thenReturn(correctedChunk);

            CorrectionResult result = textCorrectionService.correctText(task);

            assertThat(result).isNotNull();
            assertThat(result.getCorrectedText()).isEqualTo(correctedChunk);
            assertThat(result.getOptions()).isEmpty();

            assertThat(task.getStatus()).isEqualTo(TaskStatus.COMPLETED);
            assertThat(task.getCorrectedText()).isEqualTo(correctedChunk);
            assertThat(task.getErrorMessage()).isNull();

            verify(yandexSpellerService).correctText(eq(originalText), eq("EN"), anyList());
        }

        @Test
        @DisplayName("Should set status to PROCESSING at start and COMPLETED at end")
        void shouldSetStatusToProcessingThenCompleted() {
            assertThat(task.getStatus()).isEqualTo(TaskStatus.PENDING);

            when(yandexSpellerService.correctText(anyString(), anyString(), anyList()))
                    .thenReturn("Corrected text");

            textCorrectionService.correctText(task);

            assertThat(task.getStatus()).isEqualTo(TaskStatus.COMPLETED);
        }

        @Test
        @DisplayName("Should set status to FAILED and propagate exception when error occurs")
        void shouldSetStatusToFailedAndPropagateExceptionWhenErrorOccurs() {
            String errorMessage = "API connection failed";
            when(yandexSpellerService.correctText(anyString(), anyString(), anyList()))
                    .thenThrow(new RuntimeException(errorMessage));

            assertThatThrownBy(() -> textCorrectionService.correctText(task))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining(errorMessage);

            assertThat(task.getStatus()).isEqualTo(TaskStatus.FAILED);
            assertThat(task.getErrorMessage()).contains(errorMessage);
            assertThat(task.getCorrectedText()).isNull();
        }

        @Test
        @DisplayName("Should process multiple chunks and join them correctly")
        void shouldProcessMultipleChunksAndJoinThemCorrectly() {
            String longText = "a".repeat(15000);
            task.setOriginalText(longText);

            when(yandexSpellerService.correctText(anyString(), eq("EN"), anyList()))
                    .thenAnswer(invocation -> invocation.getArgument(0)); // возвращаем тот же текст

            CorrectionResult result = textCorrectionService.correctText(task);

            assertThat(result.getCorrectedText()).hasSize(15000);
            verify(yandexSpellerService,
                    atLeast(2)
            ).correctText(anyString(), eq("EN"), anyList());
        }
    }
}