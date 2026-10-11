package com.smartstudy.quizservice.dto;

import java.util.List;

public class AiQuizGenerateResponse {

    private Long quizId;
    private String title;
    private String description;
    private boolean aiGenerated;
    private List<QuestionResponse> questions;

    public AiQuizGenerateResponse() {
    }

    public AiQuizGenerateResponse(
            Long quizId,
            String title,
            String description,
            boolean aiGenerated,
            List<QuestionResponse> questions
    ) {
        this.quizId = quizId;
        this.title = title;
        this.description = description;
        this.aiGenerated = aiGenerated;
        this.questions = questions;
    }

    public Long getQuizId() {
        return quizId;
    }

    public void setQuizId(Long quizId) {
        this.quizId = quizId;
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

    public List<QuestionResponse> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionResponse> questions) {
        this.questions = questions;
    }
}