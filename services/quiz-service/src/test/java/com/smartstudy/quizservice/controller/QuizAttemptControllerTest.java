package com.smartstudy.quizservice.controller;

import com.smartstudy.quizservice.dto.QuizAttemptResponse;
import com.smartstudy.quizservice.service.QuizAttemptService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(QuizAttemptController.class)
class QuizAttemptControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private QuizAttemptService quizAttemptService;

    @Test
    void getUserAttemptsShouldReturnAttempts() throws Exception {
        Instant completedAt = Instant.parse("2026-09-30T10:00:00Z");

        QuizAttemptResponse response = new QuizAttemptResponse(
                101L,
                10L,
                8,
                10,
                80.0,
                completedAt
        );

        when(quizAttemptService.getUserAttempts(1L))
                .thenReturn(List.of(response));

        mockMvc.perform(
                        get("/api/quiz-attempts")
                                .param("userId", "1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].attemptId").value(101))
                .andExpect(jsonPath("$[0].quizId").value(10))
                .andExpect(jsonPath("$[0].score").value(8))
                .andExpect(jsonPath("$[0].totalQuestions").value(10))
                .andExpect(jsonPath("$[0].percentage").value(80.0));

        verify(quizAttemptService).getUserAttempts(1L);
    }

    @Test
    void getQuizAttemptsShouldReturnAttemptsForQuiz() throws Exception {
        Instant completedAt = Instant.parse("2026-09-30T10:00:00Z");

        QuizAttemptResponse response = new QuizAttemptResponse(
                201L,
                10L,
                9,
                10,
                90.0,
                completedAt
        );

        when(quizAttemptService.getQuizAttempts(1L, 10L))
                .thenReturn(List.of(response));

        mockMvc.perform(
                        get("/api/quiz-attempts/quiz/10")
                                .param("userId", "1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].attemptId").value(201))
                .andExpect(jsonPath("$[0].quizId").value(10))
                .andExpect(jsonPath("$[0].score").value(9))
                .andExpect(jsonPath("$[0].totalQuestions").value(10))
                .andExpect(jsonPath("$[0].percentage").value(90.0));

        verify(quizAttemptService).getQuizAttempts(1L, 10L);
    }

    @Test
    void getUserAttemptsShouldReturnEmptyListWhenNoAttemptsExist() throws Exception {
        when(quizAttemptService.getUserAttempts(1L))
                .thenReturn(List.of());

        mockMvc.perform(
                        get("/api/quiz-attempts")
                                .param("userId", "1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        verify(quizAttemptService).getUserAttempts(1L);
    }
}