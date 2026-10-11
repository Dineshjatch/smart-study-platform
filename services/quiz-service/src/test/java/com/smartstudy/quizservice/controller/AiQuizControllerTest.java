package com.smartstudy.quizservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartstudy.quizservice.dto.AiQuizGenerateRequest;
import com.smartstudy.quizservice.dto.AiQuizGenerateResponse;
import com.smartstudy.quizservice.dto.QuestionResponse;
import com.smartstudy.quizservice.service.AiQuizService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AiQuizController.class)
class AiQuizControllerTest {

    @org.springframework.beans.factory.annotation.Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AiQuizService aiQuizService;

    @Test
    void generateQuizShouldReturnGeneratedQuiz() throws Exception {

        AiQuizGenerateRequest request = new AiQuizGenerateRequest();
        request.setTopic("Java Collections");
        request.setNumberOfQuestions(2);
        request.setDifficulty("medium");

        QuestionResponse question = new QuestionResponse(
                501L,
                "Which interface stores unique elements?",
                "List",
                "Set",
                "Queue",
                "Map"
        );

        AiQuizGenerateResponse response = new AiQuizGenerateResponse(
                100L,
                "Java Collections Quiz",
                "A quiz about Java Collections",
                true,
                List.of(question)
        );

        when(aiQuizService.generateQuiz(
                eq(1L),
                any(AiQuizGenerateRequest.class)
        )).thenReturn(response);

        mockMvc.perform(
                        post("/api/ai-quizzes/generate")
                                .param("userId", "1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quizId").value(100))
                .andExpect(jsonPath("$.title").value("Java Collections Quiz"))
                .andExpect(jsonPath("$.description")
                        .value("A quiz about Java Collections"))
                .andExpect(jsonPath("$.aiGenerated").value(true))
                .andExpect(jsonPath("$.questions[0].id").value(501))
                .andExpect(jsonPath("$.questions[0].questionText")
                        .value("Which interface stores unique elements?"))
                .andExpect(jsonPath("$.questions[0].optionA").value("List"))
                .andExpect(jsonPath("$.questions[0].optionB").value("Set"))
                .andExpect(jsonPath("$.questions[0].correctAnswer").doesNotExist());

        verify(aiQuizService).generateQuiz(
                eq(1L),
                any(AiQuizGenerateRequest.class)
        );
    }

    @Test
    void generateQuizShouldReturnBadRequestForMissingTopic() throws Exception {

        String requestJson = """
                {
                  "numberOfQuestions": 5,
                  "difficulty": "medium"
                }
                """;

        mockMvc.perform(
                        post("/api/ai-quizzes/generate")
                                .param("userId", "1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isBadRequest());

        verify(aiQuizService, never()).generateQuiz(
                eq(1L),
                any(AiQuizGenerateRequest.class)
        );
    }

    @Test
    void generateQuizShouldReturnBadRequestForInvalidQuestionCount() throws Exception {

        String requestJson = """
                {
                  "topic": "Java",
                  "numberOfQuestions": 25,
                  "difficulty": "medium"
                }
                """;

        mockMvc.perform(
                        post("/api/ai-quizzes/generate")
                                .param("userId", "1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isBadRequest());

        verify(aiQuizService, never()).generateQuiz(
                eq(1L),
                any(AiQuizGenerateRequest.class)
        );
    }

    @Test
    void generateQuizShouldReturnBadRequestForMissingDifficulty() throws Exception {

        String requestJson = """
                {
                  "topic": "Java",
                  "numberOfQuestions": 5
                }
                """;

        mockMvc.perform(
                        post("/api/ai-quizzes/generate")
                                .param("userId", "1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isBadRequest());

        verify(aiQuizService, never()).generateQuiz(
                eq(1L),
                any(AiQuizGenerateRequest.class)
        );
    }
}