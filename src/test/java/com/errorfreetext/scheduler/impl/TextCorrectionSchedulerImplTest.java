package com.errorfreetext.scheduler.impl;

import com.errorfreetext.dto.CorrectionResult;
import com.errorfreetext.entity.Task;
import com.errorfreetext.entity.enums.TaskStatus;
import com.errorfreetext.service.TaskService;
import com.errorfreetext.service.TextCorrectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TextCorrectionSchedulerImpl Unit Tests")
class TextCorrectionSchedulerImplTest {

    @Mock
    private TaskService taskService;

    @Mock
    private TextCorrectionService correctionService;

    @InjectMocks
    private TextCorrectionSchedulerImpl scheduler;

    private UUID taskId;
    private Task pendingTask;
    private Task processingTask;
    private CorrectionResult correctionResult;

    @BeforeEach
    void setUp() {
        taskId = UUID.randomUUID();

        pendingTask = Task.builder()
                .id(taskId)
                .originalText("Helo world")
                .status(TaskStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        processingTask = Task.builder()
                .id(taskId)
                .originalText("Helo world")
                .status(TaskStatus.PROCESSING)
                .createdAt(LocalDateTime.now().minusMinutes(35))
                .updatedAt(LocalDateTime.now().minusMinutes(35))
                .build();

        correctionResult = new CorrectionResult("Hello world", List.of());
    }

    @Nested
    @DisplayName("processPendingTasks() method tests")
    class ProcessPendingTasksTests {

        @Test
        @DisplayName("Should process pending tasks when they exist")
        void shouldProcessPendingTasksWhenTheyExist() {
            List<Task> pendingTasks = List.of(pendingTask);
            when(taskService.findPendingTasks(10)).thenReturn(pendingTasks);
            when(correctionService.correctText(pendingTask)).thenReturn(correctionResult);

            scheduler.processPendingTasks();

            verify(taskService).findPendingTasks(10);
            verify(taskService).markAsProcessing(taskId);
            verify(correctionService).correctText(pendingTask);
            verify(taskService).markAsCompleted(eq(taskId), eq("Hello world"), eq(List.of()));
            verify(taskService, never()).markAsFailed(any(), anyString());
        }

        @Test
        @DisplayName("Should do nothing when no pending tasks exist")
        void shouldDoNothingWhenNoPendingTasksExist() {
            when(taskService.findPendingTasks(10)).thenReturn(List.of());

            scheduler.processPendingTasks();

            verify(taskService).findPendingTasks(10);
            verify(taskService, never()).markAsProcessing(any());
            verify(correctionService, never()).correctText(any());
            verify(taskService, never()).markAsCompleted(any(), any(), any());
            verify(taskService, never()).markAsFailed(any(), any());
        }

        @Test
        @DisplayName("Should mark task as failed when correction throws exception")
        void shouldMarkTaskAsFailedWhenCorrectionThrowsException() {
            List<Task> pendingTasks = List.of(pendingTask);
            when(taskService.findPendingTasks(10)).thenReturn(pendingTasks);
            doThrow(new RuntimeException("API error")).when(correctionService).correctText(pendingTask);

            scheduler.processPendingTasks();

            verify(taskService).findPendingTasks(10);
            verify(taskService).markAsProcessing(taskId);
            verify(correctionService).correctText(pendingTask);
            verify(taskService, never()).markAsCompleted(any(), any(), any());
            verify(taskService).markAsFailed(eq(taskId), eq("API error"));
        }

        @Test
        @DisplayName("Should mark task as failed with unknown error when exception has null message")
        void shouldMarkTaskAsFailedWithUnknownErrorWhenExceptionHasNullMessage() {
            List<Task> pendingTasks = List.of(pendingTask);
            when(taskService.findPendingTasks(10)).thenReturn(pendingTasks);
            doThrow(new RuntimeException()).when(correctionService).correctText(pendingTask);

            scheduler.processPendingTasks();

            verify(taskService).findPendingTasks(10);
            verify(taskService).markAsProcessing(taskId);
            verify(correctionService).correctText(pendingTask);
            verify(taskService, never()).markAsCompleted(any(), any(), any());
            verify(taskService).markAsFailed(eq(taskId), eq("Unknown error"));
        }

        @Test
        @DisplayName("Should process multiple tasks independently when one fails")
        void shouldProcessMultipleTasksIndependentlyWhenOneFails() {
            UUID taskId1 = UUID.randomUUID();
            UUID taskId2 = UUID.randomUUID();

            Task task1 = Task.builder().id(taskId1).originalText("Text 1").status(TaskStatus.PENDING).build();
            Task task2 = Task.builder().id(taskId2).originalText("Text 2").status(TaskStatus.PENDING).build();

            List<Task> pendingTasks = List.of(task1, task2);
            when(taskService.findPendingTasks(10)).thenReturn(pendingTasks);

            doThrow(new RuntimeException("Task 1 failed")).when(correctionService).correctText(task1);
            when(correctionService.correctText(task2)).thenReturn(correctionResult);

            scheduler.processPendingTasks();

            verify(taskService).markAsProcessing(taskId1);
            verify(taskService).markAsProcessing(taskId2);
            verify(taskService).markAsFailed(eq(taskId1), eq("Task 1 failed"));
            verify(taskService).markAsCompleted(eq(taskId2), eq("Hello world"), eq(List.of()));
        }

        @Test
        @DisplayName("Should process tasks in order they are received")
        void shouldProcessTasksInOrderTheyAreReceived() {
            UUID taskId1 = UUID.randomUUID();
            UUID taskId2 = UUID.randomUUID();

            Task task1 = Task.builder().id(taskId1).originalText("First").status(TaskStatus.PENDING).build();
            Task task2 = Task.builder().id(taskId2).originalText("Second").status(TaskStatus.PENDING).build();

            List<Task> pendingTasks = List.of(task1, task2);
            when(taskService.findPendingTasks(10)).thenReturn(pendingTasks);
            when(correctionService.correctText(any())).thenReturn(correctionResult);

            scheduler.processPendingTasks();

            InOrder inOrder = inOrder(taskService);
            inOrder.verify(taskService).markAsProcessing(taskId1);
            inOrder.verify(taskService).markAsCompleted(eq(taskId1), any(), any());
            inOrder.verify(taskService).markAsProcessing(taskId2);
            inOrder.verify(taskService).markAsCompleted(eq(taskId2), any(), any());
        }
    }

    @Nested
    @DisplayName("recoverStuckTasks() method tests")
    class RecoverStuckTasksTests {

        @Test
        @DisplayName("Should recover stuck tasks when they exist")
        void shouldRecoverStuckTasksWhenTheyExist() {
            List<Task> stuckTasks = List.of(processingTask);
            when(taskService.findStuckTasks(30)).thenReturn(stuckTasks);

            scheduler.recoverStuckTasks();

            verify(taskService).findStuckTasks(30);
            verify(taskService).markAsFailed(
                    eq(taskId),
                    eq("Task processing timeout after 30 minutes")
            );
        }

        @Test
        @DisplayName("Should do nothing when no stuck tasks exist")
        void shouldDoNothingWhenNoStuckTasksExist() {
            when(taskService.findStuckTasks(30)).thenReturn(List.of());

            scheduler.recoverStuckTasks();

            verify(taskService).findStuckTasks(30);
            verify(taskService, never()).markAsFailed(any(), anyString());
        }

        @Test
        @DisplayName("Should recover multiple stuck tasks")
        void shouldRecoverMultipleStuckTasks() {
            UUID taskId1 = UUID.randomUUID();
            UUID taskId2 = UUID.randomUUID();

            Task stuckTask1 = Task.builder().id(taskId1).status(TaskStatus.PROCESSING).build();
            Task stuckTask2 = Task.builder().id(taskId2).status(TaskStatus.PROCESSING).build();

            List<Task> stuckTasks = List.of(stuckTask1, stuckTask2);
            when(taskService.findStuckTasks(30)).thenReturn(stuckTasks);

            scheduler.recoverStuckTasks();

            verify(taskService, times(2)).markAsFailed(
                    any(UUID.class),
                    eq("Task processing timeout after 30 minutes")
            );
        }

        @Test
        @DisplayName("Should use correct timeout value of 30 minutes")
        void shouldUseCorrectTimeoutValue() {
            when(taskService.findStuckTasks(30)).thenReturn(List.of());

            scheduler.recoverStuckTasks();

            verify(taskService).findStuckTasks(eq(30L));
        }
    }
}