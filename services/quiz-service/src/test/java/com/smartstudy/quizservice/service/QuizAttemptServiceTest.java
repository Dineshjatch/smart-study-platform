package com.smartstudy.quizservice.service;

import com.smartstudy.quizservice.dto.QuizAttemptResponse;
import com.smartstudy.quizservice.model.QuizAttempt;
import com.smartstudy.quizservice.repository.QuizAttemptRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuizAttemptServiceTest {

    @Mock
    private QuizAttemptRepository quizAttemptRepository;

    @InjectMocks
    private QuizAttemptService quizAttemptService;

    @Test
    void getUserAttemptsShouldReturnAttemptsInRepositoryOrder() {
        Instant firstTime = Instant.parse("2026-09-30T10:00:00Z");
        Instant secondTime = Instant.parse("2026-09-29T10:00:00Z");

        QuizAttempt attempt1 =
                new QuizAttempt(1L, 10L, 8, 10, firstTime);

        QuizAttempt attempt2 =
                new QuizAttempt(1L, 20L, 5, 5, secondTime);

        setAttemptId(attempt1, 101L);
        setAttemptId(attempt2, 102L);

        when(quizAttemptRepository.findByUserIdOrderByCompletedAtDesc(1L))
                .thenReturn(List.of(attempt1, attempt2));

        List<QuizAttemptResponse> results =
                quizAttemptService.getUserAttempts(1L);

        assertEquals(2, results.size());

        assertEquals(101L, results.get(0).getAttemptId());
        assertEquals(10L, results.get(0).getQuizId());
        assertEquals(8, results.get(0).getScore());
        assertEquals(10, results.get(0).getTotalQuestions());
        assertEquals(80.0, results.get(0).getPercentage());

        assertEquals(102L, results.get(1).getAttemptId());
        assertEquals(20L, results.get(1).getQuizId());
        assertEquals(5, results.get(1).getScore());
        assertEquals(5, results.get(1).getTotalQuestions());
        assertEquals(100.0, results.get(1).getPercentage());

        verify(quizAttemptRepository)
                .findByUserIdOrderByCompletedAtDesc(1L);
    }

    @Test
    void getQuizAttemptsShouldReturnOnlyAttemptsForRequestedQuiz() {
        Instant completedAt = Instant.parse("2026-09-30T10:00:00Z");

        QuizAttempt attempt1 =
                new QuizAttempt(1L, 10L, 7, 10, completedAt);

        QuizAttempt attempt2 =
                new QuizAttempt(1L, 10L, 9, 10, completedAt.minusSeconds(3600));

        setAttemptId(attempt1, 201L);
        setAttemptId(attempt2, 202L);

        when(quizAttemptRepository
                .findByUserIdAndQuizIdOrderByCompletedAtDesc(1L, 10L))
                .thenReturn(List.of(attempt1, attempt2));

        List<QuizAttemptResponse> results =
                quizAttemptService.getQuizAttempts(1L, 10L);

        assertEquals(2, results.size());

        assertEquals(201L, results.get(0).getAttemptId());
        assertEquals(10L, results.get(0).getQuizId());
        assertEquals(7, results.get(0).getScore());
        assertEquals(70.0, results.get(0).getPercentage());

        assertEquals(202L, results.get(1).getAttemptId());
        assertEquals(10L, results.get(1).getQuizId());
        assertEquals(9, results.get(1).getScore());
        assertEquals(90.0, results.get(1).getPercentage());

        verify(quizAttemptRepository)
                .findByUserIdAndQuizIdOrderByCompletedAtDesc(1L, 10L);
    }

    @Test
    void getUserAttemptsShouldReturnEmptyListWhenNoAttemptsExist() {
        when(quizAttemptRepository.findByUserIdOrderByCompletedAtDesc(1L))
                .thenReturn(List.of());

        List<QuizAttemptResponse> results =
                quizAttemptService.getUserAttempts(1L);

        assertEquals(0, results.size());

        verify(quizAttemptRepository)
                .findByUserIdOrderByCompletedAtDesc(1L);
    }

    private void setAttemptId(QuizAttempt attempt, Long id) {
        try {
            var field = QuizAttempt.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(attempt, id);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Could not set test attempt ID", e);
        }
    }
}