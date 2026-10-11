package com.smartstudy.quizservice.service;

import com.smartstudy.quizservice.dto.QuizAttemptResponse;
import com.smartstudy.quizservice.model.QuizAttempt;
import com.smartstudy.quizservice.repository.QuizAttemptRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuizAttemptService {

    private final QuizAttemptRepository quizAttemptRepository;

    public QuizAttemptService(QuizAttemptRepository quizAttemptRepository) {
        this.quizAttemptRepository = quizAttemptRepository;
    }

    public List<QuizAttemptResponse> getUserAttempts(Long userId) {
        return quizAttemptRepository
                .findByUserIdOrderByCompletedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<QuizAttemptResponse> getQuizAttempts(Long userId, Long quizId) {
        return quizAttemptRepository
                .findByUserIdAndQuizIdOrderByCompletedAtDesc(userId, quizId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private QuizAttemptResponse toResponse(QuizAttempt attempt) {

        double percentage = 0.0;

        if (attempt.getTotalQuestions() > 0) {
            percentage =
                    ((double) attempt.getScore() / attempt.getTotalQuestions()) * 100.0;
        }

        return new QuizAttemptResponse(
                attempt.getId(),
                attempt.getQuizId(),
                attempt.getScore(),
                attempt.getTotalQuestions(),
                percentage,
                attempt.getCompletedAt()
        );
    }
}