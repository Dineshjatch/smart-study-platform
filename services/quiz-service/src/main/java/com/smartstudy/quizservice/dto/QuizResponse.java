package com.smartstudy.quizservice.dto;

import java.time.Instant;

public class QuizResponse {

    private Long id;
    private String title;
    private String description;
    private boolean aiGenerated;
    private Instant createdAt;

    public QuizResponse() {
    }

    public QuizResponse(
            Long id,
            String title,
            String description,
            boolean aiGenerated,
            Instant createdAt) {

        this.id = id;
        this.title = title;
        this.description = description;
        this.aiGenerated = aiGenerated;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public boolean isAiGenerated() {
        return aiGenerated;
    }

    public void setAiGenerated(boolean aiGenerated) {
        this.aiGenerated = aiGenerated;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}