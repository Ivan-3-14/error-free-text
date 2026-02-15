package com.errorfreetext.service.impl;

import com.errorfreetext.dto.CreateTaskRequest;
import com.errorfreetext.dto.TaskResponse;
import com.errorfreetext.entity.Task;
import com.errorfreetext.entity.enums.Language;
import com.errorfreetext.entity.enums.TaskStatus;
import com.errorfreetext.exception.TaskNotFoundException;
import com.errorfreetext.exception.ValidationException;
import com.errorfreetext.repository.TaskRepository;
import com.errorfreetext.service.TaskService;
import com.errorfreetext.service.ValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service implementation for managing text correction tasks.
 * Provides operations for creating, retrieving, updating, and cleaning up tasks
 * throughout their lifecycle (PENDING → PROCESSING → COMPLETED/FAILED).
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ValidationService validationService;

    /**
     * Creates a new task for text correction and persists it in the database.
     * The task is initially created with PENDING status.
     *
     * @param request the request containing the text to be corrected and its language
     * @return a {@link TaskResponse} containing the created task's details, including its generated ID
     * @throws ValidationException if the text fails validation (e.g., contains only special characters)
     */
    @Override
    public TaskResponse createTask(CreateTaskRequest request) {
        log.info("Creating new task with language: {}", request.getLanguage());
        validationService.validateTextContent(request.getText());

        Task task = Task.builder().id(UUID.randomUUID()).originalText(request.getText()).language(Language.valueOf(request.getLanguage().toUpperCase())).status(TaskStatus.PENDING).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();

        Task savedTask = taskRepository.save(task);
        log.info("Task created with id: {}", savedTask.getId());
        return mapToResponse(savedTask);
    }

    /**
     * Retrieves a task by its unique identifier.
     *
     * @param id the UUID of the task to retrieve
     * @return a {@link TaskResponse} containing the task's current state
     * @throws TaskNotFoundException if no task exists with the given ID
     */
    @Transactional(readOnly = true)
    @Override
    public TaskResponse getTask(UUID id) {
        log.debug("Fetching task with id: {}", id);

        Task task = taskRepository.findById(id).orElseThrow(() -> new TaskNotFoundException(id));

        return mapToResponse(task);
    }

    /**
     * Retrieves a limited number of pending tasks for processing, ordered by creation date (oldest first).
     * Used by the scheduler to fetch tasks that need text correction.
     *
     * @param limit the maximum number of tasks to retrieve
     * @return a list of {@link Task} entities with PENDING status
     */
    @Transactional(readOnly = true)
    @Override
    public List<Task> findPendingTasks(int limit) {
        log.debug("Fetching {} pending tasks", limit);

        Pageable pageable = PageRequest.of(0, limit);
        return taskRepository.findByStatusOrderByCreatedAtAsc(TaskStatus.PENDING, pageable);
    }

    /**
     * Identifies tasks that have been stuck in PROCESSING state for longer than the specified timeout.
     * These tasks may have encountered errors during correction and need to be recovered.
     *
     * @param timeoutMinutes the timeout threshold in minutes; tasks updated before this time are considered stuck
     * @return a list of {@link Task} entities that have been in PROCESSING state for too long
     */
    @Transactional(readOnly = true)
    @Override
    public List<Task> findStuckTasks(long timeoutMinutes) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(timeoutMinutes);
        return taskRepository.findByStatusAndUpdatedAtBefore(TaskStatus.PROCESSING, cutoffTime);
    }

    /**
     * Updates the task status to PROCESSING to indicate that text correction has started.
     * This prevents the same task from being picked up by other scheduler instances.
     *
     * @param id the UUID of the task to mark as processing
     * @throws TaskNotFoundException if no task exists with the given ID
     */
    @Override
    public void markAsProcessing(UUID id) {
        log.debug("Marking task {} as processing", id);

        Task task = taskRepository.findById(id).orElseThrow(() -> new TaskNotFoundException(id));

        task.setStatus(TaskStatus.PROCESSING);
        task.setUpdatedAt(LocalDateTime.now());
        taskRepository.save(task);
    }

    /**
     * Updates the task with the corrected text and marks it as COMPLETED.
     * This method is called when text correction has finished successfully.
     *
     * @param id            the UUID of the task to mark as completed
     * @param correctedText the corrected version of the original text
     * @param options       the list of Yandex Speller options that were applied during correction
     * @throws TaskNotFoundException if no task exists with the given ID
     */
    @Override
    public void markAsCompleted(UUID id, String correctedText, List<String> options) {
        log.info("Marking task {} as completed", id);

        Task task = taskRepository.findById(id).orElseThrow(() -> new TaskNotFoundException(id));

        task.setStatus(TaskStatus.COMPLETED);

        taskRepository.save(Task.builder().id(task.getId()).createdAt(task.getCreatedAt()).originalText(task.getOriginalText()).language(task.getLanguage()).errorMessage(null).options(options).correctedText(correctedText).status(TaskStatus.COMPLETED).updatedAt(LocalDateTime.now()).build());
    }

    /**
     * Marks the task as FAILED and stores the error message for debugging.
     * This method is called when text correction encounters an unrecoverable error.
     *
     * @param id           the UUID of the task to mark as failed
     * @param errorMessage a description of the error that occurred during correction
     * @throws TaskNotFoundException if no task exists with the given ID
     */
    @Override
    public void markAsFailed(UUID id, String errorMessage) {
        log.error("Marking task {} as failed: {}", id, errorMessage);

        Task task = taskRepository.findById(id).orElseThrow(() -> new TaskNotFoundException(id));

        task.setStatus(TaskStatus.FAILED);
        task.setErrorMessage(errorMessage);
        task.setUpdatedAt(LocalDateTime.now());

        taskRepository.save(task);
    }

    /**
     * Maps a Task entity to its corresponding TaskResponse DTO.
     * This internal method ensures that sensitive or internal fields are not exposed in the API response.
     *
     * @param task the Task entity to map
     * @return a {@link TaskResponse} containing the public task data
     */
    private TaskResponse mapToResponse(Task task) {
        return TaskResponse.builder().id(task.getId()).status(task.getStatus().name()).correctedText(task.getCorrectedText()).errorMessage(task.getErrorMessage()).createdAt(task.getCreatedAt()).updatedAt(task.getUpdatedAt()).build();
    }
}