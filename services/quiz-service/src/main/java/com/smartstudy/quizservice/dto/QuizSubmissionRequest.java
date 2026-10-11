package com.smartstudy.quizservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

public class QuizSubmissionRequest {

    @NotNull(message = "Quiz ID is required")
    private Long quizId;

    @Size(min = 1, message = "At least one answer is required")
    @Valid
    private List<AnswerSubmission> answers = new ArrayList<>();

    public QuizSubmissionRequest() {
    }

    public Long getQuizId() {
        return quizId;
    }

    public void setQuizId(Long quizId) {
        this.quizId = quizId;
    }

    public List<AnswerSubmission> getAnswers() {
        return answers;
    }

    public void setAnswers(List<AnswerSubmission> answers) {
        this.answers = answers;
    }

    public static class AnswerSubmission {

        @NotNull(message = "Question ID is required")
        private Long questionId;

        @NotNull(message = "Selected answer is required")
        @Pattern(
                regexp = "^[ABCDabcd]$",
                message = "Selected answer must be A, B, C, or D"
        )
        private String selectedAnswer;

        public AnswerSubmission() {
        }

        public Long getQuestionId() {
            return questionId;
        }

        public void setQuestionId(Long questionId) {
            this.questionId = questionId;
        }

        public String getSelectedAnswer() {
            return selectedAnswer;
        }

        public void setSelectedAnswer(String selectedAnswer) {
            this.selectedAnswer = selectedAnswer;
        }
    }
}