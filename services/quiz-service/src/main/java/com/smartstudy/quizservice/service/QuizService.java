package com.smartstudy.quizservice.service;

import com.smartstudy.quizservice.dto.QuizRequest;
import com.smartstudy.quizservice.dto.QuizResponse;
import com.smartstudy.quizservice.model.Quiz;
import com.smartstudy.quizservice.repository.QuestionRepository;
import com.smartstudy.quizservice.repository.QuizRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;

    public QuizService(
            QuizRepository quizRepository,
            QuestionRepository questionRepository
    ) {
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
    }

    public QuizResponse createQuiz(
            Long userId,
            QuizRequest request) {

        Quiz quiz = new Quiz(
                userId,
                request.getTitle(),
                request.getDescription(),
                false,
                Instant.now()
        );

        Quiz savedQuiz = quizRepository.save(quiz);

        return toResponse(savedQuiz);
    }

    public List<QuizResponse> getQuizzesByUser(Long userId) {

        return quizRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public QuizResponse getQuiz(
            Long userId,
            Long quizId) {

        Quiz quiz = findOwnedQuiz(userId, quizId);

        return toResponse(quiz);
    }

    public QuizResponse updateQuiz(
            Long userId,
            Long quizId,
            QuizRequest request) {

        Quiz quiz = findOwnedQuiz(userId, quizId);

        quiz.setTitle(request.getTitle());
        quiz.setDescription(request.getDescription());

        Quiz updatedQuiz = quizRepository.save(quiz);

        return toResponse(updatedQuiz);
    }

    @Transactional
    public void deleteQuiz(
            Long userId,
            Long quizId) {

        Quiz quiz = findOwnedQuiz(userId, quizId);

        // Delete questions first so no orphaned question rows remain.
        questionRepository.deleteByQuizId(quizId);

        quizRepository.delete(quiz);
    }

    private Quiz findOwnedQuiz(
            Long userId,
            Long quizId) {

        Quiz quiz = quizRepository
                .findByIdAndUserId(quizId, userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Quiz not found"
                        )
                );

        return quiz;
    }

    private QuizResponse toResponse(Quiz quiz) {

        return new QuizResponse(
                quiz.getId(),
                quiz.getTitle(),
                quiz.getDescription(),
                quiz.isAiGenerated(),
                quiz.getCreatedAt()
        );
    }
}