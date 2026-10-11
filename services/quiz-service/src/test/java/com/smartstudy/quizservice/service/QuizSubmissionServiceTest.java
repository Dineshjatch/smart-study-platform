package com.smartstudy.quizservice.service;

import com.smartstudy.quizservice.dto.QuizResultResponse;
import com.smartstudy.quizservice.dto.QuizSubmissionRequest;
import com.smartstudy.quizservice.model.Question;
import com.smartstudy.quizservice.model.QuizAttempt;
import com.smartstudy.quizservice.repository.QuestionRepository;
import com.smartstudy.quizservice.repository.QuizAnswerRepository;
import com.smartstudy.quizservice.repository.QuizAttemptRepository;
import com.smartstudy.quizservice.repository.QuizRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class QuizSubmissionServiceTest {

    @Test
    void submitQuizShouldCalculateScoreFromStoredCorrectAnswers() {

        QuizRepository quizRepository = mock(QuizRepository.class);
        QuestionRepository questionRepository =
                mock(QuestionRepository.class);
        QuizAttemptRepository quizAttemptRepository =
                mock(QuizAttemptRepository.class);
        QuizAnswerRepository quizAnswerRepository =
                mock(QuizAnswerRepository.class);

        when(quizRepository.findByIdAndUserId(10L, 1L))
                .thenReturn(Optional.of(
                        new com.smartstudy.quizservice.model.Quiz(
                                1L,
                                "Java Basics",
                                "Java fundamentals",
                                false,
                                Instant.now()
                        )
                ));

        Question question1 = new Question(
                10L,
                "What does JVM stand for?",
                "Java Virtual Machine",
                "Java Version Manager",
                "Java Visual Machine",
                "Java Variable Method",
                "A"
        );
        setQuestionId(question1, 1L);

        Question question2 = new Question(
                10L,
                "Which keyword creates a new object?",
                "class",
                "new",
                "static",
                "void",
                "B"
        );
        setQuestionId(question2, 2L);

        Question question3 = new Question(
                10L,
                "Which type stores true or false?",
                "int",
                "String",
                "boolean",
                "double",
                "C"
        );
        setQuestionId(question3, 3L);

        when(questionRepository.findByQuizId(10L))
                .thenReturn(List.of(
                        question1,
                        question2,
                        question3
                ));

        QuizAttempt savedAttempt = new QuizAttempt(
                1L,
                10L,
                2,
                3,
                Instant.now()
        );

        // The generated ID is not available through a setter,
        // so this test uses reflection to simulate a database-generated ID.
        setAttemptId(savedAttempt, 100L);

        when(quizAttemptRepository.save(any(QuizAttempt.class)))
                .thenReturn(savedAttempt);

        QuizSubmissionRequest request =
                new QuizSubmissionRequest();

        request.setQuizId(10L);

        QuizSubmissionRequest.AnswerSubmission answer1 =
                new QuizSubmissionRequest.AnswerSubmission();

        answer1.setQuestionId(1L);
        answer1.setSelectedAnswer("A");

        QuizSubmissionRequest.AnswerSubmission answer2 =
                new QuizSubmissionRequest.AnswerSubmission();

        answer2.setQuestionId(2L);
        answer2.setSelectedAnswer("B");

        QuizSubmissionRequest.AnswerSubmission answer3 =
                new QuizSubmissionRequest.AnswerSubmission();

        answer3.setQuestionId(3L);
        answer3.setSelectedAnswer("A");

        request.setAnswers(List.of(
                answer1,
                answer2,
                answer3
        ));

        QuizSubmissionService service =
                new QuizSubmissionService(
                        quizRepository,
                        questionRepository,
                        quizAttemptRepository,
                        quizAnswerRepository
                );

        QuizResultResponse result =
                service.submitQuiz(1L, request);

        assertEquals(100L, result.getAttemptId());
        assertEquals(10L, result.getQuizId());
        assertEquals(2, result.getScore());
        assertEquals(3, result.getTotalQuestions());
//        assertEquals(66.66666666666667, result.getPercentage());
        assertEquals(66.66666666666666, result.getPercentage());

        verify(quizRepository)
                .findByIdAndUserId(10L, 1L);

        verify(questionRepository)
                .findByQuizId(10L);

        verify(quizAttemptRepository)
                .save(any(QuizAttempt.class));

        verify(quizAnswerRepository, times(3))
                .save(any(com.smartstudy.quizservice.model.QuizAnswer.class));
    }

    @Test
    void submitQuizShouldRejectDuplicateQuestionAnswers() {

        QuizRepository quizRepository = mock(QuizRepository.class);
        QuestionRepository questionRepository =
                mock(QuestionRepository.class);
        QuizAttemptRepository quizAttemptRepository =
                mock(QuizAttemptRepository.class);
        QuizAnswerRepository quizAnswerRepository =
                mock(QuizAnswerRepository.class);

        when(quizRepository.findByIdAndUserId(10L, 1L))
                .thenReturn(Optional.of(
                        new com.smartstudy.quizservice.model.Quiz(
                                1L,
                                "Java Basics",
                                "Java fundamentals",
                                false,
                                Instant.now()
                        )
                ));

        Question question = new Question(
                10L,
                "What does JVM stand for?",
                "Java Virtual Machine",
                "Java Version Manager",
                "Java Visual Machine",
                "Java Variable Method",
                "A"
        );
        setQuestionId(question, 1L);

        when(questionRepository.findByQuizId(10L))
                .thenReturn(List.of(question));

        QuizSubmissionRequest request =
                new QuizSubmissionRequest();

        request.setQuizId(10L);

        QuizSubmissionRequest.AnswerSubmission firstAnswer =
                new QuizSubmissionRequest.AnswerSubmission();

        firstAnswer.setQuestionId(1L);
        firstAnswer.setSelectedAnswer("A");

        QuizSubmissionRequest.AnswerSubmission secondAnswer =
                new QuizSubmissionRequest.AnswerSubmission();

        secondAnswer.setQuestionId(1L);
        secondAnswer.setSelectedAnswer("B");

        request.setAnswers(List.of(
                firstAnswer,
                secondAnswer
        ));

        QuizSubmissionService service =
                new QuizSubmissionService(
                        quizRepository,
                        questionRepository,
                        quizAttemptRepository,
                        quizAnswerRepository
                );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.submitQuiz(1L, request)
        );

        assertEquals(
                "A question was answered more than once",
                exception.getMessage()
        );

        verify(quizAttemptRepository, never())
                .save(any(QuizAttempt.class));

        verify(quizAnswerRepository, never())
                .save(any(com.smartstudy.quizservice.model.QuizAnswer.class));
    }

    @Test
    void submitQuizShouldRejectQuestionFromAnotherQuiz() {

        QuizRepository quizRepository = mock(QuizRepository.class);
        QuestionRepository questionRepository =
                mock(QuestionRepository.class);
        QuizAttemptRepository quizAttemptRepository =
                mock(QuizAttemptRepository.class);
        QuizAnswerRepository quizAnswerRepository =
                mock(QuizAnswerRepository.class);

        when(quizRepository.findByIdAndUserId(10L, 1L))
                .thenReturn(Optional.of(
                        new com.smartstudy.quizservice.model.Quiz(
                                1L,
                                "Java Basics",
                                "Java fundamentals",
                                false,
                                Instant.now()
                        )
                ));

        Question question = new Question(
                10L,
                "What does JVM stand for?",
                "Java Virtual Machine",
                "Java Version Manager",
                "Java Visual Machine",
                "Java Variable Method",
                "A"
        );
        setQuestionId(question, 1L);

        when(questionRepository.findByQuizId(10L))
                .thenReturn(List.of(question));

        QuizSubmissionRequest request =
                new QuizSubmissionRequest();

        request.setQuizId(10L);

        QuizSubmissionRequest.AnswerSubmission answer =
                new QuizSubmissionRequest.AnswerSubmission();

        // Question 999 does not belong to quiz 10.
        answer.setQuestionId(999L);
        answer.setSelectedAnswer("A");

        request.setAnswers(List.of(answer));

        QuizSubmissionService service =
                new QuizSubmissionService(
                        quizRepository,
                        questionRepository,
                        quizAttemptRepository,
                        quizAnswerRepository
                );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.submitQuiz(1L, request)
        );

        assertEquals(
                "Question does not belong to this quiz",
                exception.getMessage()
        );

        verify(quizAttemptRepository, never())
                .save(any(QuizAttempt.class));
    }

    @Test
    void submitQuizShouldRejectMissingAnswers() {

        QuizRepository quizRepository = mock(QuizRepository.class);
        QuestionRepository questionRepository =
                mock(QuestionRepository.class);
        QuizAttemptRepository quizAttemptRepository =
                mock(QuizAttemptRepository.class);
        QuizAnswerRepository quizAnswerRepository =
                mock(QuizAnswerRepository.class);

        when(quizRepository.findByIdAndUserId(10L, 1L))
                .thenReturn(Optional.of(
                        new com.smartstudy.quizservice.model.Quiz(
                                1L,
                                "Java Basics",
                                "Java fundamentals",
                                false,
                                Instant.now()
                        )
                ));

        Question question1 = new Question(
                10L,
                "Question 1",
                "A",
                "B",
                "C",
                "D",
                "A"
        );
        setQuestionId(question1, 1L);

        Question question2 = new Question(
                10L,
                "Question 2",
                "A",
                "B",
                "C",
                "D",
                "B"
        );
        setQuestionId(question2, 2L);

        when(questionRepository.findByQuizId(10L))
                .thenReturn(List.of(
                        question1,
                        question2
                ));

        QuizSubmissionRequest request =
                new QuizSubmissionRequest();

        request.setQuizId(10L);

        QuizSubmissionRequest.AnswerSubmission answer =
                new QuizSubmissionRequest.AnswerSubmission();

        answer.setQuestionId(1L);
        answer.setSelectedAnswer("A");

        request.setAnswers(List.of(answer));

        QuizSubmissionService service =
                new QuizSubmissionService(
                        quizRepository,
                        questionRepository,
                        quizAttemptRepository,
                        quizAnswerRepository
                );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.submitQuiz(1L, request)
        );

        assertEquals(
                "All quiz questions must be answered",
                exception.getMessage()
        );

        verify(quizAttemptRepository, never())
                .save(any(QuizAttempt.class));
    }

    @Test
    void submitQuizShouldRejectQuizWithoutQuestions() {

        QuizRepository quizRepository = mock(QuizRepository.class);
        QuestionRepository questionRepository =
                mock(QuestionRepository.class);
        QuizAttemptRepository quizAttemptRepository =
                mock(QuizAttemptRepository.class);
        QuizAnswerRepository quizAnswerRepository =
                mock(QuizAnswerRepository.class);

        when(quizRepository.findByIdAndUserId(10L, 1L))
                .thenReturn(Optional.of(
                        new com.smartstudy.quizservice.model.Quiz(
                                1L,
                                "Empty Quiz",
                                "No questions",
                                false,
                                Instant.now()
                        )
                ));

        when(questionRepository.findByQuizId(10L))
                .thenReturn(List.of());

        QuizSubmissionRequest request =
                new QuizSubmissionRequest();

        request.setQuizId(10L);
        request.setAnswers(List.of());

        QuizSubmissionService service =
                new QuizSubmissionService(
                        quizRepository,
                        questionRepository,
                        quizAttemptRepository,
                        quizAnswerRepository
                );

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.submitQuiz(1L, request)
        );

        assertEquals(
                "Quiz does not contain any questions",
                exception.getMessage()
        );

        verify(quizAttemptRepository, never())
                .save(any(QuizAttempt.class));

        verify(quizAnswerRepository, never())
                .save(any(com.smartstudy.quizservice.model.QuizAnswer.class));
    }


    private void setQuestionId(Question question, Long id) {
        try {
            java.lang.reflect.Field field =
                    Question.class.getDeclaredField("id");

            field.setAccessible(true);
            field.set(question, id);

        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(
                    "Could not prepare test question ID",
                    exception
            );
        }
    }

    private void setAttemptId(QuizAttempt attempt, Long id) {

        try {
            java.lang.reflect.Field field =
                    QuizAttempt.class.getDeclaredField("id");

            field.setAccessible(true);
            field.set(attempt, id);

        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(
                    "Could not prepare test attempt ID",
                    exception
            );
        }
    }
}