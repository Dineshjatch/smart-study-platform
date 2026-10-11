package com.smartstudy.quizservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class QuizRequest {

    @NotBlank(message = "Quiz title is required")
    @Size(max = 100, message = "Quiz title cannot exceed 100 characters")
    private String title;

    @Size(max = 500, message = "Quiz description cannot exceed 500 characters")
    private String description;

    public QuizRequest() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}