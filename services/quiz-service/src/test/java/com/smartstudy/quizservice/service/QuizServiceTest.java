package com.smartstudy.quizservice.service;

import com.smartstudy.quizservice.dto.QuizRequest;
import com.smartstudy.quizservice.dto.QuizResponse;
import com.smartstudy.quizservice.model.Quiz;
import com.smartstudy.quizservice.repository.QuestionRepository;
import com.smartstudy.quizservice.repository.QuizRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class QuizServiceTest {

    private final QuizRepository quizRepository =
            mock(QuizRepository.class);

    private final QuestionRepository questionRepository =
            mock(QuestionRepository.class);

    private final QuizService quizService =
            new QuizService(
                    quizRepository,
                    questionRepository
            );

    @Test
    void createQuizShouldSaveManualQuiz() {

        QuizRequest request = new QuizRequest();
        request.setTitle("Java Basics");
        request.setDescription("Basic Java quiz");

        Quiz savedQuiz = new Quiz(
                1L,
                "Java Basics",
                "Basic Java quiz",
                false,
                Instant.now()
        );

        setQuizId(savedQuiz, 100L);

        when(quizRepository.save(any(Quiz.class)))
                .thenReturn(savedQuiz);

        QuizResponse response =
                quizService.createQuiz(1L, request);

        assertEquals(100L, response.getId());
        assertEquals("Java Basics", response.getTitle());
        assertEquals("Basic Java quiz", response.getDescription());
        assertEquals(false, response.isAiGenerated());

        verify(quizRepository).save(any(Quiz.class));
        verifyNoInteractions(questionRepository);
    }

    @Test
    void getQuizzesByUserShouldReturnUserQuizzes() {

        Quiz quiz1 = new Quiz(
                1L,
                "Java Basics",
                "Basic Java quiz",
                false,
                Instant.parse("2026-09-30T10:00:00Z")
        );

        Quiz quiz2 = new Quiz(
                1L,
                "Spring Boot",
                "Spring Boot quiz",
                true,
                Instant.parse("2026-09-29T10:00:00Z")
        );

        setQuizId(quiz1, 101L);
        setQuizId(quiz2, 102L);

        when(quizRepository.findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(quiz1, quiz2));

        List<QuizResponse> results =
                quizService.getQuizzesByUser(1L);

        assertEquals(2, results.size());

        assertEquals(101L, results.get(0).getId());
        assertEquals("Java Basics", results.get(0).getTitle());
        assertEquals(false, results.get(0).isAiGenerated());

        assertEquals(102L, results.get(1).getId());
        assertEquals("Spring Boot", results.get(1).getTitle());
        assertEquals(true, results.get(1).isAiGenerated());

        verify(quizRepository)
                .findByUserIdOrderByCreatedAtDesc(1L);

        verifyNoInteractions(questionRepository);
    }

    @Test
    void getQuizShouldReturnOwnedQuiz() {

        Quiz quiz = new Quiz(
                1L,
                "Java Basics",
                "Basic Java quiz",
                false,
                Instant.now()
        );

        setQuizId(quiz, 100L);

        when(quizRepository.findByIdAndUserId(100L, 1L))
                .thenReturn(Optional.of(quiz));

        QuizResponse response =
                quizService.getQuiz(1L, 100L);

        assertEquals(100L, response.getId());
        assertEquals("Java Basics", response.getTitle());
        assertEquals("Basic Java quiz", response.getDescription());

        verify(quizRepository)
                .findByIdAndUserId(100L, 1L);

        verifyNoInteractions(questionRepository);
    }

    @Test
    void getQuizShouldRejectQuizNotOwnedByUser() {

        when(quizRepository.findByIdAndUserId(100L, 1L))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> quizService.getQuiz(1L, 100L)
                );

        assertEquals("Quiz not found", exception.getMessage());

        verify(quizRepository)
                .findByIdAndUserId(100L, 1L);

        verifyNoInteractions(questionRepository);
    }

    @Test
    void updateQuizShouldUpdateOwnedQuiz() {

        Quiz quiz = new Quiz(
                1L,
                "Old Title",
                "Old description",
                false,
                Instant.now()
        );

        setQuizId(quiz, 100L);

        when(quizRepository.findByIdAndUserId(100L, 1L))
                .thenReturn(Optional.of(quiz));

        when(quizRepository.save(any(Quiz.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        QuizRequest request = new QuizRequest();
        request.setTitle("New Title");
        request.setDescription("New description");

        QuizResponse response =
                quizService.updateQuiz(
                        1L,
                        100L,
                        request
                );

        assertEquals(100L, response.getId());
        assertEquals("New Title", response.getTitle());
        assertEquals("New description", response.getDescription());

        verify(quizRepository)
                .findByIdAndUserId(100L, 1L);

        verify(quizRepository)
                .save(any(Quiz.class));

        verifyNoInteractions(questionRepository);
    }

    @Test
    void updateQuizShouldRejectQuizNotOwnedByUser() {

        when(quizRepository.findByIdAndUserId(100L, 1L))
                .thenReturn(Optional.empty());

        QuizRequest request = new QuizRequest();
        request.setTitle("New Title");
        request.setDescription("New description");

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> quizService.updateQuiz(
                                1L,
                                100L,
                                request
                        )
                );

        assertEquals("Quiz not found", exception.getMessage());

        verify(quizRepository)
                .findByIdAndUserId(100L, 1L);

        verify(quizRepository, never())
                .save(any(Quiz.class));

        verifyNoInteractions(questionRepository);
    }

    @Test
    void deleteQuizShouldDeleteQuestionsAndQuiz() {

        Quiz quiz = new Quiz(
                1L,
                "Java Basics",
                "Basic Java quiz",
                false,
                Instant.now()
        );

        setQuizId(quiz, 100L);

        when(quizRepository.findByIdAndUserId(100L, 1L))
                .thenReturn(Optional.of(quiz));

        quizService.deleteQuiz(1L, 100L);

        verify(questionRepository)
                .deleteByQuizId(100L);

        verify(quizRepository)
                .delete(quiz);
    }

    @Test
    void deleteQuizShouldRejectQuizNotOwnedByUser() {

        when(quizRepository.findByIdAndUserId(100L, 1L))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> quizService.deleteQuiz(1L, 100L)
                );

        assertEquals("Quiz not found", exception.getMessage());

        verify(questionRepository, never())
                .deleteByQuizId(anyLong());

        verify(quizRepository, never())
                .delete(any(Quiz.class));
    }

    private void setQuizId(Quiz quiz, Long id) {
        try {
            var field = Quiz.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(quiz, id);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(
                    "Could not set test quiz ID",
                    e
            );
        }
    }
}