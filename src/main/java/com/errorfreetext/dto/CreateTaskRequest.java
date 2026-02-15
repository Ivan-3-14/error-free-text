package com.errorfreetext.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request DTO for creating a new text correction task.
 * <p>
 * Contains the text to be corrected and its language specification.
 * Both fields are required and validated at the controller level.
 * </p>
 */
@Data
public class CreateTaskRequest {

    /**
     * Тhe text to be corrected (min 3 characters, cannot be only special chars)
     */
    @NotBlank(message = "Text cannot be empty")
    @Size(min = 3, message = "Text must contain at least 3 characters")
    @Schema(description = "Text to be corrected", example = "Helo world! How are yuo?")
    private String text;

    /**
     * Тhe language of the text (must be either "RU" or "EN")
     */
    @NotBlank(message = "Language is required")
    @Pattern(regexp = "^(RU|EN)$", message = "Language must be either RU or EN")
    @Schema(description = "Language of the text", example = "EN", allowableValues = {"RU", "EN"})
    private String language;
}