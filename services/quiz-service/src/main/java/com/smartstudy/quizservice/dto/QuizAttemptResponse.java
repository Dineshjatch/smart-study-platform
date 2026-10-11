package com.smartstudy.quizservice.dto;

import java.time.Instant;

public class QuizAttemptResponse {

    private Long attemptId;
    private Long quizId;
    private int score;
    private int totalQuestions;
    private double percentage;
    private Instant completedAt;

    public QuizAttemptResponse() {
    }

    public QuizAttemptResponse(
            Long attemptId,
            Long quizId,
            int score,
            int totalQuestions,
            double percentage,
            Instant completedAt
    ) {
        this.attemptId = attemptId;
        this.quizId = quizId;
        this.score = score;
        this.totalQuestions = totalQuestions;
        this.percentage = percentage;
        this.completedAt = completedAt;
    }

    public Long getAttemptId() {
        return attemptId;
    }

    public void setAttemptId(Long attemptId) {
        this.attemptId = attemptId;
    }

    public Long getQuizId() {
        return quizId;
    }

    public void setQuizId(Long quizId) {
        this.quizId = quizId;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }
}