package com.smartstudy.quizservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartstudy.quizservice.dto.QuizResultResponse;
import com.smartstudy.quizservice.dto.QuizSubmissionRequest;
import com.smartstudy.quizservice.service.QuizSubmissionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(QuizSubmissionController.class)
class QuizSubmissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private QuizSubmissionService quizSubmissionService;

    @Test
    void submitQuizShouldReturnResult() throws Exception {
        QuizSubmissionRequest request = new QuizSubmissionRequest();
        request.setQuizId(10L);

        QuizSubmissionRequest.AnswerSubmission answer1 =
                new QuizSubmissionRequest.AnswerSubmission();
        answer1.setQuestionId(1L);
        answer1.setSelectedAnswer("A");

        QuizSubmissionRequest.AnswerSubmission answer2 =
                new QuizSubmissionRequest.AnswerSubmission();
        answer2.setQuestionId(2L);
        answer2.setSelectedAnswer("B");

        request.setAnswers(java.util.List.of(answer1, answer2));

        Instant completedAt = Instant.now();

        QuizResultResponse response = new QuizResultResponse(
                100L,
                10L,
                2,
                2,
                100.0,
                completedAt
        );

        when(quizSubmissionService.submitQuiz(
                eq(1L),
                any(QuizSubmissionRequest.class)
        )).thenReturn(response);

        mockMvc.perform(
                        post("/api/quiz-submissions")
                                .param("userId", "1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attemptId").value(100))
                .andExpect(jsonPath("$.quizId").value(10))
                .andExpect(jsonPath("$.score").value(2))
                .andExpect(jsonPath("$.totalQuestions").value(2))
                .andExpect(jsonPath("$.percentage").value(100.0));

        verify(quizSubmissionService).submitQuiz(
                eq(1L),
                any(QuizSubmissionRequest.class)
        );
    }

    @Test
    void submitQuizShouldReturnBadRequestForMissingQuizId() throws Exception {
        String requestJson = """
                {
                  "answers": [
                    {
                      "questionId": 1,
                      "selectedAnswer": "A"
                    }
                  ]
                }
                """;

        mockMvc.perform(
                        post("/api/quiz-submissions")
                                .param("userId", "1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isBadRequest());

        verify(quizSubmissionService, never()).submitQuiz(
                eq(1L),
                any(QuizSubmissionRequest.class)
        );
    }

    @Test
    void submitQuizShouldReturnBadRequestForEmptyAnswers() throws Exception {
        String requestJson = """
                {
                  "quizId": 10,
                  "answers": []
                }
                """;

        mockMvc.perform(
                        post("/api/quiz-submissions")
                                .param("userId", "1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isBadRequest());

        verify(quizSubmissionService, never()).submitQuiz(
                eq(1L),
                any(QuizSubmissionRequest.class)
        );
    }

    @Test
    void submitQuizShouldReturnBadRequestForInvalidSelectedAnswer() throws Exception {
        String requestJson = """
                {
                  "quizId": 10,
                  "answers": [
                    {
                      "questionId": 1,
                      "selectedAnswer": "X"
                    }
                  ]
                }
                """;

        mockMvc.perform(
                        post("/api/quiz-submissions")
                                .param("userId", "1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isBadRequest());

        verify(quizSubmissionService, never()).submitQuiz(
                eq(1L),
                any(QuizSubmissionRequest.class)
        );
    }
}