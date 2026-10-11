package com.smartstudy.quizservice.service;

import com.smartstudy.quizservice.dto.QuestionRequest;
import com.smartstudy.quizservice.dto.QuestionResponse;
import com.smartstudy.quizservice.model.Question;
import com.smartstudy.quizservice.repository.QuestionRepository;
import com.smartstudy.quizservice.repository.QuizRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class QuestionServiceTest {

    @Test
    void addQuestionShouldSaveQuestionAndNormalizeCorrectAnswer() {

        QuestionRepository questionRepository =
                mock(QuestionRepository.class);

        QuizRepository quizRepository =
                mock(QuizRepository.class);

        when(quizRepository.findByIdAndUserId(10L, 1L))
                .thenReturn(Optional.of(
                        new com.smartstudy.quizservice.model.Quiz(
                                1L,
                                "Java Basics",
                                "Java fundamentals",
                                false,
                                java.time.Instant.now()
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

        when(questionRepository.save(any(Question.class)))
                .thenReturn(question);

        QuestionService questionService =
                new QuestionService(
                        questionRepository,
                        quizRepository
                );

        QuestionRequest request = new QuestionRequest();
        request.setQuestionText("What does JVM stand for?");
        request.setOptionA("Java Virtual Machine");
        request.setOptionB("Java Version Manager");
        request.setOptionC("Java Visual Machine");
        request.setOptionD("Java Variable Method");
        request.setCorrectAnswer("a");

        QuestionResponse response =
                questionService.addQuestion(1L, 10L, request);

        assertEquals(
                "What does JVM stand for?",
                response.getQuestionText()
        );

        assertEquals(
                "Java Virtual Machine",
                response.getOptionA()
        );

        // QuestionResponse intentionally has no correctAnswer field.
        assertNotNull(response);

        verify(quizRepository)
                .findByIdAndUserId(10L, 1L);

        verify(questionRepository)
                .save(any(Question.class));
    }

    @Test
    void getQuestionsShouldReturnQuestionsWithoutCorrectAnswer() {

        QuestionRepository questionRepository =
                mock(QuestionRepository.class);

        QuizRepository quizRepository =
                mock(QuizRepository.class);

        when(quizRepository.findByIdAndUserId(10L, 1L))
                .thenReturn(Optional.of(
                        new com.smartstudy.quizservice.model.Quiz(
                                1L,
                                "Java Basics",
                                "Java fundamentals",
                                false,
                                java.time.Instant.now()
                        )
                ));

        Question question = new Question(
                10L,
                "Which language runs on the JVM?",
                "Java",
                "HTML",
                "CSS",
                "SQL",
                "A"
        );

        when(questionRepository.findByQuizId(10L))
                .thenReturn(List.of(question));

        QuestionService questionService =
                new QuestionService(
                        questionRepository,
                        quizRepository
                );

        List<QuestionResponse> questions =
                questionService.getQuestions(1L, 10L);

        assertEquals(1, questions.size());

        QuestionResponse response = questions.get(0);

        assertEquals(
                "Which language runs on the JVM?",
                response.getQuestionText()
        );

        assertEquals("Java", response.getOptionA());
        assertEquals("HTML", response.getOptionB());
        assertEquals("CSS", response.getOptionC());
        assertEquals("SQL", response.getOptionD());

        verify(quizRepository)
                .findByIdAndUserId(10L, 1L);

        verify(questionRepository)
                .findByQuizId(10L);
    }

    @Test
    void getQuestionsShouldRejectQuizOwnedByAnotherUser() {

        QuestionRepository questionRepository =
                mock(QuestionRepository.class);

        QuizRepository quizRepository =
                mock(QuizRepository.class);

        // User 1 tries to access quiz 10, but the ownership-aware
        // repository query says it does not belong to user 1.
        when(quizRepository.findByIdAndUserId(10L, 1L))
                .thenReturn(Optional.empty());

        QuestionService questionService =
                new QuestionService(
                        questionRepository,
                        quizRepository
                );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> questionService.getQuestions(1L, 10L)
        );

        assertEquals(
                "Quiz not found",
                exception.getMessage()
        );

        // No question query should happen after ownership fails.
        verify(questionRepository, never())
                .findByQuizId(anyLong());
    }

    @Test
    void addQuestionShouldRejectQuizOwnedByAnotherUser() {

        QuestionRepository questionRepository =
                mock(QuestionRepository.class);

        QuizRepository quizRepository =
                mock(QuizRepository.class);

        when(quizRepository.findByIdAndUserId(10L, 1L))
                .thenReturn(Optional.empty());

        QuestionService questionService =
                new QuestionService(
                        questionRepository,
                        quizRepository
                );

        QuestionRequest request = new QuestionRequest();
        request.setQuestionText("What is Java?");
        request.setOptionA("Language");
        request.setOptionB("Database");
        request.setOptionC("Operating System");
        request.setOptionD("Browser");
        request.setCorrectAnswer("A");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> questionService.addQuestion(
                        1L,
                        10L,
                        request
                )
        );

        assertEquals(
                "Quiz not found",
                exception.getMessage()
        );

        verify(questionRepository, never())
                .save(any(Question.class));
    }
}