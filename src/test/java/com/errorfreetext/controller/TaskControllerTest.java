package com.errorfreetext.controller;

import com.errorfreetext.dto.CreateTaskRequest;
import com.errorfreetext.dto.TaskResponse;
import com.errorfreetext.exception.TaskNotFoundException;
import com.errorfreetext.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
@DisplayName("TaskController Unit Tests")
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;

    private UUID taskId;
    private TaskResponse taskResponse;
    private CreateTaskRequest validRequest;
    private CreateTaskRequest invalidRequest;

    @BeforeEach
    void setUp() {
        taskId = UUID.randomUUID();

        taskResponse = TaskResponse.builder()
                .id(taskId)
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        validRequest = new CreateTaskRequest();
        validRequest.setText("Hello world");
        validRequest.setLanguage("EN");

        invalidRequest = new CreateTaskRequest();
        invalidRequest.setText(""); // empty text
        invalidRequest.setLanguage("EN");
    }

    @Nested
    @DisplayName("POST /api/v1/tasks - createTask()")
    class CreateTaskTests {

        @Test
        @DisplayName("Should return 201 CREATED with task response when request is valid")
        void shouldReturn201Created_WhenRequestIsValid() throws Exception {
            when(taskService.createTask(any(CreateTaskRequest.class))).thenReturn(taskResponse);

            mockMvc.perform(post("/api/v1/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(taskId.toString()))
                    .andExpect(jsonPath("$.status").value("PENDING"))
                    .andExpect(jsonPath("$.correctedText").doesNotExist())
                    .andExpect(jsonPath("$.errorMessage").doesNotExist())
                    .andExpect(jsonPath("$.createdAt").exists())
                    .andExpect(jsonPath("$.updatedAt").exists());
        }

        @Test
        @DisplayName("Should return 500  when text is empty")
        void shouldReturn400BadRequest_WhenTextIsEmpty() throws Exception {
            mockMvc.perform(post("/api/v1/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andDo(print())
                    .andExpect(status().is5xxServerError());
        }

        @Test
        @DisplayName("Should return 500 when text is too short")
        void shouldReturn400BadRequest_WhenTextIsTooShort() throws Exception {
            CreateTaskRequest shortTextRequest = new CreateTaskRequest();
            shortTextRequest.setText("a");
            shortTextRequest.setLanguage("EN");

            mockMvc.perform(post("/api/v1/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(shortTextRequest)))
                    .andDo(print())
                    .andExpect(status().is5xxServerError());
        }

        @Test
        @DisplayName("Should return 500 when language is invalid")
        void shouldReturn400BadRequestWhenLanguageIsInvalid() throws Exception {
            CreateTaskRequest invalidLangRequest = new CreateTaskRequest();
            invalidLangRequest.setText("Hello world");
            invalidLangRequest.setLanguage("FR");

            mockMvc.perform(post("/api/v1/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidLangRequest)))
                    .andDo(print())
                    .andExpect(status().is5xxServerError());
        }

        @Test
        @DisplayName("Should return 500 when request body is missing")
        void shouldReturn400BadRequestWhenRequestBodyIsMissing() throws Exception {
            mockMvc.perform(post("/api/v1/tasks")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().is5xxServerError());
        }

        @Test
        @DisplayName("Should return 500 INTERNAL SERVER ERROR when service throws exception")
        void shouldReturn500InternalServerErrorWhenServiceThrowsException() throws Exception {
            when(taskService.createTask(any(CreateTaskRequest.class)))
                    .thenThrow(new RuntimeException("Database error"));

            mockMvc.perform(post("/api/v1/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andDo(print())
                    .andExpect(status().isInternalServerError());
        }

        @Test
        @DisplayName("Should handle different languages correctly")
        void shouldHandleDifferentLanguagesCorrectly() throws Exception {
            CreateTaskRequest ruRequest = new CreateTaskRequest();
            ruRequest.setText("Привет мир");
            ruRequest.setLanguage("RU");

            when(taskService.createTask(any(CreateTaskRequest.class))).thenReturn(taskResponse);

            mockMvc.perform(post("/api/v1/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(ruRequest)))
                    .andDo(print())
                    .andExpect(status().isCreated());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/tasks/{id} - getTask()")
    class GetTaskTests {

        @Test
        @DisplayName("Should return 200 OK with task response when task exists")
        void shouldReturn200OkWhenTaskExists() throws Exception {
            when(taskService.getTask(taskId)).thenReturn(taskResponse);

            mockMvc.perform(get("/api/v1/tasks/{id}", taskId))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(taskId.toString()))
                    .andExpect(jsonPath("$.status").value("PENDING"));
        }

        @Test
        @DisplayName("Should return 200 OK with corrected text when task is COMPLETED")
        void shouldReturn200OkWithCorrectedTextWhenTaskIsCompleted() throws Exception {
            TaskResponse completedResponse = TaskResponse.builder()
                    .id(taskId)
                    .status("COMPLETED")
                    .correctedText("Hello world!")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            when(taskService.getTask(taskId)).thenReturn(completedResponse);

            mockMvc.perform(get("/api/v1/tasks/{id}", taskId))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("COMPLETED"))
                    .andExpect(jsonPath("$.correctedText").value("Hello world!"))
                    .andExpect(jsonPath("$.errorMessage").doesNotExist());
        }

        @Test
        @DisplayName("Should return 200 OK with error message when task is FAILED")
        void shouldReturn200OkWithErrorMessageWhenTaskIsFailed() throws Exception {
            TaskResponse failedResponse = TaskResponse.builder()
                    .id(taskId)
                    .status("FAILED")
                    .errorMessage("API timeout")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            when(taskService.getTask(taskId)).thenReturn(failedResponse);

            mockMvc.perform(get("/api/v1/tasks/{id}", taskId))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("FAILED"))
                    .andExpect(jsonPath("$.errorMessage").value("API timeout"))
                    .andExpect(jsonPath("$.correctedText").doesNotExist());
        }

        @Test
        @DisplayName("Should return 404 NOT FOUND when task does not exist")
        void shouldReturn404NotFoundWhenTaskDoesNotExist() throws Exception {
            UUID nonExistentId = UUID.randomUUID();
            when(taskService.getTask(nonExistentId))
                    .thenThrow(new TaskNotFoundException(nonExistentId));

            mockMvc.perform(get("/api/v1/tasks/{id}", nonExistentId))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 500 INTERNAL SERVER ERROR when service throws exception")
        void shouldReturn500InternalServerErrorWhenServiceThrowsException() throws Exception {
            when(taskService.getTask(taskId)).thenThrow(new RuntimeException("Database error"));

            mockMvc.perform(get("/api/v1/tasks/{id}", taskId))
                    .andDo(print())
                    .andExpect(status().isInternalServerError());
        }

        @Test
        @DisplayName("Should return 500  when ID format is invalid")
        void shouldReturn400BadRequestWhenIdFormatIsInvalid() throws Exception {
            mockMvc.perform(get("/api/v1/tasks/invalid-uuid-format"))
                    .andDo(print())
                    .andExpect(status().is5xxServerError());
        }
    }

    @Nested
    @DisplayName("Controller Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should handle both create and get operations in sequence")
        void shouldHandleCreateAndGetInSequence() throws Exception {
            when(taskService.createTask(any(CreateTaskRequest.class))).thenReturn(taskResponse);

            String createResponse = mockMvc.perform(post("/api/v1/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(taskId.toString()))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            TaskResponse createdTask = objectMapper.readValue(createResponse, TaskResponse.class);

            when(taskService.getTask(createdTask.getId())).thenReturn(taskResponse);

            mockMvc.perform(get("/api/v1/tasks/{id}", createdTask.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(createdTask.getId().toString()));
        }
    }
}
