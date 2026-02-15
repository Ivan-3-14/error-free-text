package com.errorfreetext.controller;

import com.errorfreetext.dto.CreateTaskRequest;
import com.errorfreetext.dto.TaskResponse;
import com.errorfreetext.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for managing text correction tasks.
 * <p>
 * Provides endpoints for creating new correction tasks and retrieving their results.
 * All responses follow the standard {@link TaskResponse} format, with errors
 * handled globally via {@link com.errorfreetext.exception.GlobalExceptionHandler}.
 * </p>
 *
 * <p>
 * <b>Base URL:</b> {@code /api/v1/tasks}
 * </p>
 *
 * @see com.errorfreetext.service.TaskService
 * @see com.errorfreetext.dto.TaskResponse
 * @see com.errorfreetext.dto.CreateTaskRequest
 */

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Text correction task management")
public class TaskController {

    /**
     * Service layer for task operations.
     * Handles business logic including task creation, retrieval, and status management.
     */
    private final TaskService taskService;

    /**
     * Creates a new text correction task.
     * <p>
     * Accepts a text and its language, validates the input, creates a task
     * with PENDING status, and returns the generated task ID for tracking.
     * The task will be processed asynchronously by the scheduler.
     * </p>
     *
     * <p>
     * <b>Request example:</b>
     * <pre>
     * {
     *   "text": "Helo world! How are yuo?",
     *   "language": "EN"
     * }
     * </pre>
     * </p>
     *
     * <p>
     * <b>Response example (201 Created):</b>
     * <pre>
     * {
     *   "id": "550e8400-e29b-41d4-a716-446655440000",
     *   "status": "PENDING",
     *   "createdAt": "2026-02-15T12:00:00.123",
     *   "updatedAt": "2026-02-15T12:00:00.123"
     * }
     * </pre>
     * </p>
     *
     * @param taskRequest the request containing the text to be corrected and its language
     * @return a {@link ResponseEntity} containing:
     * <ul>
     *   <li>201 CREATED with {@link TaskResponse} if successful</li>
     *   <li>400 BAD REQUEST if validation fails</li>
     *   <li>500 INTERNAL SERVER ERROR if unexpected error occurs</li>
     * </ul>
     */
    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody CreateTaskRequest taskRequest) {
        TaskResponse response = taskService.createTask(taskRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves a task by its unique identifier.
     * <p>
     * Returns the current state of the task. If the task is COMPLETED,
     * the corrected text is included in the response. If the task is FAILED,
     * an error message explains what went wrong.
     * </p>
     *
     * <p>
     * <b>Response examples by status:</b>
     * <ul>
     *   <li><b>COMPLETED:</b> includes correctedText field</li>
     *   <li><b>PENDING/PROCESSING:</b> no correctedText field</li>
     *   <li><b>FAILED:</b> includes errorMessage field</li>
     * </ul>
     * </p>
     *
     * @param id the UUID of the task to retrieve
     * @return a {@link ResponseEntity} containing:
     * <ul>
     *   <li>200 OK with {@link TaskResponse} if task exists</li>
     *   <li>404 NOT FOUND if task doesn't exist</li>
     *   <li>500 INTERNAL SERVER ERROR if unexpected error occurs</li>
     * </ul>
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get task by ID")
    public ResponseEntity<TaskResponse> getTask(@PathVariable UUID id) {
        TaskResponse response = taskService.getTask(id);
        return ResponseEntity.ok(response);
    }
}