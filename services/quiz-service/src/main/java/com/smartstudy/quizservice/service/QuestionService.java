package com.smartstudy.quizservice.service;

import com.smartstudy.quizservice.dto.QuestionRequest;
import com.smartstudy.quizservice.dto.QuestionResponse;
import com.smartstudy.quizservice.model.Question;
import com.smartstudy.quizservice.repository.QuestionRepository;
import com.smartstudy.quizservice.repository.QuizRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final QuizRepository quizRepository;

    public QuestionService(
            QuestionRepository questionRepository,
            QuizRepository quizRepository) {

        this.questionRepository = questionRepository;
        this.quizRepository = quizRepository;
    }

    public QuestionResponse addQuestion(
            Long userId,
            Long quizId,
            QuestionRequest request) {

        verifyQuizOwnership(userId, quizId);

        Question question = new Question(
                quizId,
                request.getQuestionText(),
                request.getOptionA(),
                request.getOptionB(),
                request.getOptionC(),
                request.getOptionD(),
                request.getCorrectAnswer().toUpperCase()
        );

        Question savedQuestion = questionRepository.save(question);

        return toResponse(savedQuestion);
    }

    public List<QuestionResponse> getQuestions(
            Long userId,
            Long quizId) {

        verifyQuizOwnership(userId, quizId);

        return questionRepository.findByQuizId(quizId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private void verifyQuizOwnership(
            Long userId,
            Long quizId) {

        boolean quizBelongsToUser = quizRepository
                .findByIdAndUserId(quizId, userId)
                .isPresent();

        if (!quizBelongsToUser) {
            throw new IllegalArgumentException(
                    "Quiz not found"
            );
        }
    }

    private QuestionResponse toResponse(Question question) {

        return new QuestionResponse(
                question.getId(),
                question.getQuestionText(),
                question.getOptionA(),
                question.getOptionB(),
                question.getOptionC(),
                question.getOptionD()
        );
    }
}