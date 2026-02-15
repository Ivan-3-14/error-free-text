package com.errorfreetext.service.impl;

import com.errorfreetext.dto.CreateTaskRequest;
import com.errorfreetext.dto.TaskResponse;
import com.errorfreetext.entity.Task;
import com.errorfreetext.entity.enums.Language;
import com.errorfreetext.entity.enums.TaskStatus;
import com.errorfreetext.exception.TaskNotFoundException;
import com.errorfreetext.exception.ValidationException;
import com.errorfreetext.repository.TaskRepository;
import com.errorfreetext.service.ValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskServiceImpl Unit Tests")
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ValidationService validationService;

    @InjectMocks
    private TaskServiceImpl taskService;

    @Captor
    private ArgumentCaptor<Task> taskCaptor;

    private UUID taskId;
    private Task task;
    private CreateTaskRequest createTaskRequest;

    @BeforeEach
    void setUp() {
        taskId = UUID.randomUUID();

        task = Task.builder()
                .id(taskId)
                .originalText("Hello world")
                .language(Language.EN)
                .status(TaskStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        createTaskRequest = new CreateTaskRequest();
        createTaskRequest.setText("Hello world");
        createTaskRequest.setLanguage("EN");
    }

    @Nested
    @DisplayName("createTask() method tests")
    class CreateTaskTests {

        @Test
        @DisplayName("Should create task successfully when request is valid")
        void shouldCreateTaskWhenRequestIsValid() {
            doNothing().when(validationService).validateTextContent(createTaskRequest.getText());
            when(taskRepository.save(any(Task.class))).thenReturn(task);
            TaskResponse response = taskService.createTask(createTaskRequest);
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(taskId);
            assertThat(response.getStatus()).isEqualTo(TaskStatus.PENDING.name());
            assertThat(response.getCorrectedText()).isNull();
            assertThat(response.getErrorMessage()).isNull();

            verify(validationService).validateTextContent(createTaskRequest.getText());
            verify(taskRepository).save(taskCaptor.capture());

            Task capturedTask = taskCaptor.getValue();
            assertThat(capturedTask.getOriginalText()).isEqualTo("Hello world");
            assertThat(capturedTask.getLanguage()).isEqualTo(Language.EN);
            assertThat(capturedTask.getStatus()).isEqualTo(TaskStatus.PENDING);
            assertThat(capturedTask.getId()).isNotNull();
            assertThat(capturedTask.getCreatedAt()).isNotNull();
            assertThat(capturedTask.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should throw ValidationException when text validation fails")
        void shouldThrowValidationExceptionWhenTextValidationFails() {
            doThrow(new ValidationException("Invalid text"))
                    .when(validationService).validateTextContent(createTaskRequest.getText());

            assertThatThrownBy(() -> taskService.createTask(createTaskRequest))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Invalid text");

            verify(validationService).validateTextContent(createTaskRequest.getText());
            verify(taskRepository, never()).save(any(Task.class));
        }

        @Test
        @DisplayName("Should convert language to uppercase correctly")
        void shouldConvertLanguageToUppercase() {
            createTaskRequest.setLanguage("en"); // lowercase
            doNothing().when(validationService).validateTextContent(createTaskRequest.getText());
            when(taskRepository.save(any(Task.class))).thenReturn(task);

            taskService.createTask(createTaskRequest);

            verify(taskRepository).save(taskCaptor.capture());
            assertThat(taskCaptor.getValue().getLanguage()).isEqualTo(Language.EN);
        }
    }

    @Nested
    @DisplayName("getTask() method tests")
    class GetTaskTests {

        @Test
        @DisplayName("Should return task response when task exists")
        void shouldReturnTaskResponseWhenTaskExists() {
            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

            TaskResponse response = taskService.getTask(taskId);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(taskId);
            assertThat(response.getStatus()).isEqualTo(TaskStatus.PENDING.name());

            verify(taskRepository).findById(taskId);
        }

        @Test
        @DisplayName("Should throw TaskNotFoundException when task does not exist")
        void shouldThrowTaskNotFoundExceptionWhenTaskDoesNotExist() {
            when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> taskService.getTask(taskId))
                    .isInstanceOf(TaskNotFoundException.class)
                    .hasMessageContaining(taskId.toString());

            verify(taskRepository).findById(taskId);
        }
    }

    @Nested
    @DisplayName("findPendingTasks() method tests")
    class FindPendingTasksTests {

        @Test
        @DisplayName("Should return list of pending tasks ordered by creation date")
        void shouldReturnPendingTasksOrderedByCreationDate() {
            int limit = 5;
            Pageable expectedPageable = PageRequest.of(0, limit);
            List<Task> pendingTasks = List.of(task);

            when(taskRepository.findByStatusOrderByCreatedAtAsc(TaskStatus.PENDING, expectedPageable))
                    .thenReturn(pendingTasks);

            List<Task> result = taskService.findPendingTasks(limit);

            assertThat(result).isNotEmpty();
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStatus()).isEqualTo(TaskStatus.PENDING);

            verify(taskRepository).findByStatusOrderByCreatedAtAsc(TaskStatus.PENDING, expectedPageable);
        }

        @Test
        @DisplayName("Should return empty list when no pending tasks exist")
        void shouldReturnEmptyListWhenNoPendingTasks() {
            int limit = 5;
            Pageable expectedPageable = PageRequest.of(0, limit);

            when(taskRepository.findByStatusOrderByCreatedAtAsc(TaskStatus.PENDING, expectedPageable))
                    .thenReturn(List.of());

            List<Task> result = taskService.findPendingTasks(limit);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findStuckTasks() method tests")
    class FindStuckTasksTests {

        @Test
        @DisplayName("Should return tasks stuck in PROCESSING state")
        void shouldReturnStuckTasks() {
            long timeoutMinutes = 30;

            when(taskRepository.findByStatusAndUpdatedAtBefore(eq(TaskStatus.PROCESSING), any(LocalDateTime.class)))
                    .thenReturn(List.of(task));

            List<Task> result = taskService.findStuckTasks(timeoutMinutes);

            assertThat(result).isNotEmpty();
            verify(taskRepository).findByStatusAndUpdatedAtBefore(eq(TaskStatus.PROCESSING), any(LocalDateTime.class));
        }

        @Test
        @DisplayName("Should return empty list when no stuck tasks exist")
        void shouldReturnEmptyListWhenNoStuckTasks() {
            long timeoutMinutes = 30;

            when(taskRepository.findByStatusAndUpdatedAtBefore(eq(TaskStatus.PROCESSING), any(LocalDateTime.class)))
                    .thenReturn(List.of());

            List<Task> result = taskService.findStuckTasks(timeoutMinutes);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("markAsProcessing() method tests")
    class MarkAsProcessingTests {

        @Test
        @DisplayName("Should mark task as PROCESSING and update timestamp")
        void shouldMarkTaskAsProcessingAndUpdateTimestamp() throws InterruptedException {
            LocalDateTime beforeUpdate = LocalDateTime.now();
            Thread.sleep(1);

            Task processingTask = Task.builder()
                    .id(taskId)
                    .status(TaskStatus.PENDING)
                    .updatedAt(beforeUpdate)
                    .build();

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(processingTask));
            when(taskRepository.save(any(Task.class))).thenReturn(processingTask);

            taskService.markAsProcessing(taskId);

            verify(taskRepository).save(taskCaptor.capture());
            Task capturedTask = taskCaptor.getValue();

            assertThat(capturedTask.getStatus()).isEqualTo(TaskStatus.PROCESSING);
            assertThat(capturedTask.getUpdatedAt()).isAfter(beforeUpdate);
        }

        @Test
        @DisplayName("Should throw TaskNotFoundException when task does not exist")
        void shouldThrowTaskNotFoundExceptionWhenTaskDoesNotExist() {
            when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> taskService.markAsProcessing(taskId))
                    .isInstanceOf(TaskNotFoundException.class);

            verify(taskRepository, never()).save(any(Task.class));
        }
    }

    @Nested
    @DisplayName("markAsCompleted() method tests")
    class MarkAsCompletedTests {

        @Test
        @DisplayName("Should mark task as COMPLETED with corrected text and options")
        void shouldMarkTaskAsCompletedWithCorrectedTextAndOptions() {
            String correctedText = "Hello world!";
            List<String> options = List.of("IGNORE_DIGITS");

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
            when(taskRepository.save(any(Task.class))).thenReturn(task);

            taskService.markAsCompleted(taskId, correctedText, options);

            verify(taskRepository).save(taskCaptor.capture());
            Task capturedTask = taskCaptor.getValue();

            assertThat(capturedTask.getStatus()).isEqualTo(TaskStatus.COMPLETED);
            assertThat(capturedTask.getCorrectedText()).isEqualTo(correctedText);
            assertThat(capturedTask.getOptions()).isEqualTo(options);
            assertThat(capturedTask.getErrorMessage()).isNull();
            assertThat(capturedTask.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should preserve original task fields when marking as completed")
        void shouldPreserveOriginalTaskFields() {
            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
            when(taskRepository.save(any(Task.class))).thenReturn(task);

            taskService.markAsCompleted(taskId, "Corrected", List.of());

            verify(taskRepository).save(taskCaptor.capture());
            Task capturedTask = taskCaptor.getValue();

            assertThat(capturedTask.getId()).isEqualTo(task.getId());
            assertThat(capturedTask.getOriginalText()).isEqualTo(task.getOriginalText());
            assertThat(capturedTask.getLanguage()).isEqualTo(task.getLanguage());
            assertThat(capturedTask.getCreatedAt()).isEqualTo(task.getCreatedAt());
        }

        @Test
        @DisplayName("Should throw TaskNotFoundException when task does not exist")
        void shouldThrowTaskNotFoundExceptionWhenTaskDoesNotExist() {
            when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> taskService.markAsCompleted(taskId, "text", List.of()))
                    .isInstanceOf(TaskNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("markAsFailed() method tests")
    class MarkAsFailedTests {

        @Test
        @DisplayName("Should mark task as FAILED with error message")
        void shouldMarkTaskAsFailedWithErrorMessage() {
            String errorMessage = "API timeout";

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
            when(taskRepository.save(any(Task.class))).thenReturn(task);

            taskService.markAsFailed(taskId, errorMessage);

            verify(taskRepository).save(taskCaptor.capture());
            Task capturedTask = taskCaptor.getValue();

            assertThat(capturedTask.getStatus()).isEqualTo(TaskStatus.FAILED);
            assertThat(capturedTask.getErrorMessage()).isEqualTo(errorMessage);
            assertThat(capturedTask.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should throw TaskNotFoundException when task does not exist")
        void shouldThrowTaskNotFoundExceptionWhenTaskDoesNotExist() {
            when(taskRepository.findById(taskId)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> taskService.markAsFailed(taskId, "error"))
                    .isInstanceOf(TaskNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("mapToResponse() method tests")
    class MapToResponseTests {

        @Test
        @DisplayName("Should map all fields correctly when task is COMPLETED")
        void shouldMapAllFieldsCorrectlyWhenTaskIsCompleted() {
            task.setStatus(TaskStatus.COMPLETED);
            task.setCorrectedText("Hello world!");
            task.setErrorMessage(null);

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

            TaskResponse response = taskService.getTask(taskId);

            assertThat(response.getStatus()).isEqualTo(TaskStatus.COMPLETED.name());
            assertThat(response.getCorrectedText()).isEqualTo("Hello world!");
            assertThat(response.getErrorMessage()).isNull();
        }

        @Test
        @DisplayName("Should map all fields correctly when task is FAILED")
        void shouldMapAllFieldsCorrectlyWhenTaskIsFailed() {
            task.setStatus(TaskStatus.FAILED);
            task.setCorrectedText(null);
            task.setErrorMessage("API error");

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

            TaskResponse response = taskService.getTask(taskId);

            assertThat(response.getStatus()).isEqualTo(TaskStatus.FAILED.name());
            assertThat(response.getCorrectedText()).isNull();
            assertThat(response.getErrorMessage()).isEqualTo("API error");
        }
    }
}