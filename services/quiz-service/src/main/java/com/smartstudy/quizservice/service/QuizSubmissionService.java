package com.smartstudy.quizservice.service;

import com.smartstudy.quizservice.dto.QuizResultResponse;
import com.smartstudy.quizservice.dto.QuizSubmissionRequest;
import com.smartstudy.quizservice.model.Question;
import com.smartstudy.quizservice.model.QuizAttempt;
import com.smartstudy.quizservice.model.QuizAnswer;
import com.smartstudy.quizservice.repository.QuestionRepository;
import com.smartstudy.quizservice.repository.QuizAnswerRepository;
import com.smartstudy.quizservice.repository.QuizAttemptRepository;
import com.smartstudy.quizservice.repository.QuizRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class QuizSubmissionService {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final QuizAnswerRepository quizAnswerRepository;

    public QuizSubmissionService(
            QuizRepository quizRepository,
            QuestionRepository questionRepository,
            QuizAttemptRepository quizAttemptRepository,
            QuizAnswerRepository quizAnswerRepository) {

        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
        this.quizAttemptRepository = quizAttemptRepository;
        this.quizAnswerRepository = quizAnswerRepository;
    }

    @Transactional
    public QuizResultResponse submitQuiz(
            Long userId,
            QuizSubmissionRequest request) {

        Long quizId = request.getQuizId();

        // The quiz must belong to the current user.
        if (!quizRepository.findByIdAndUserId(quizId, userId).isPresent()) {
            throw new IllegalArgumentException("Quiz not found");
        }

        List<Question> questions =
                questionRepository.findByQuizId(quizId);

        if (questions.isEmpty()) {
            throw new IllegalStateException(
                    "Quiz does not contain any questions"
            );
        }

        // Map question IDs to questions so answers can be checked quickly.
        Map<Long, Question> questionsById = new HashMap<>();

        for (Question question : questions) {
            questionsById.put(question.getId(), question);
        }

        Set<Long> submittedQuestionIds = new HashSet<>();

        int score = 0;

        for (QuizSubmissionRequest.AnswerSubmission answer
                : request.getAnswers()) {

            Long questionId = answer.getQuestionId();

            // A question can only be answered once.
            if (!submittedQuestionIds.add(questionId)) {
                throw new IllegalArgumentException(
                        "A question was answered more than once"
                );
            }

            Question question = questionsById.get(questionId);

            // The submitted question must belong to this quiz.
            if (question == null) {
                throw new IllegalArgumentException(
                        "Question does not belong to this quiz"
                );
            }

            String selectedAnswer =
                    answer.getSelectedAnswer().toUpperCase();

            if (selectedAnswer.equals(question.getCorrectAnswer())) {
                score++;
            }
        }

        // Require an answer for every question in the quiz.
        if (submittedQuestionIds.size() != questions.size()) {
            throw new IllegalArgumentException(
                    "All quiz questions must be answered"
            );
        }

        Instant completedAt = Instant.now();

        QuizAttempt attempt = new QuizAttempt(
                userId,
                quizId,
                score,
                questions.size(),
                completedAt
        );

        QuizAttempt savedAttempt =
                quizAttemptRepository.save(attempt);

        // Save each selected answer for this attempt.
        for (QuizSubmissionRequest.AnswerSubmission answer
                : request.getAnswers()) {

            QuizAnswer quizAnswer = new QuizAnswer(
                    savedAttempt.getId(),
                    answer.getQuestionId(),
                    answer.getSelectedAnswer().toUpperCase()
            );

            quizAnswerRepository.save(quizAnswer);
        }

        double percentage =
                ((double) score / questions.size()) * 100.0;

        return new QuizResultResponse(
                savedAttempt.getId(),
                quizId,
                score,
                questions.size(),
                percentage,
                completedAt
        );
    }
}