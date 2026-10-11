package com.smartstudy.gateway.controller;

import com.smartstudy.gateway.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(QuizGatewayController.class)
@TestPropertySource(properties = {
        "quiz-service.url=http://localhost:8083"
})
class QuizGatewayControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RestTemplate restTemplate;

    @MockitoBean
    private JwtService jwtService;

    private void mockValidJwt() {
        when(jwtService.isTokenValid("test-jwt-token"))
                .thenReturn(true);

        when(jwtService.extractUserId("test-jwt-token"))
                .thenReturn(1L);
    }

    @Test
    void getQuizzesShouldForwardAuthenticatedUserId() throws Exception {

        mockValidJwt();

        String responseBody = """
                [
                  {
                    "id": 1,
                    "title": "Java Basics",
                    "description": "Basic Java quiz",
                    "aiGenerated": false
                  }
                ]
                """;

        when(restTemplate.exchange(
                eq("http://localhost:8083/api/quizzes?userId=1"),
                eq(HttpMethod.GET),
                any(),
                eq(String.class)
        )).thenReturn(
                ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(responseBody)
        );

        mockMvc.perform(
                        get("/api/quizzes")
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer test-jwt-token"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(content().json(responseBody));

        verify(restTemplate).exchange(
                eq("http://localhost:8083/api/quizzes?userId=1"),
                eq(HttpMethod.GET),
                any(),
                eq(String.class)
        );
    }

    @Test
    void createQuizShouldForwardAuthenticatedUserId() throws Exception {

        mockValidJwt();

        String requestBody = """
                {
                  "title": "Spring Boot",
                  "description": "Spring Boot fundamentals"
                }
                """;

        String responseBody = """
                {
                  "id": 2,
                  "title": "Spring Boot",
                  "description": "Spring Boot fundamentals",
                  "aiGenerated": false
                }
                """;

        when(restTemplate.exchange(
                eq("http://localhost:8083/api/quizzes?userId=1"),
                eq(HttpMethod.POST),
                any(),
                eq(String.class)
        )).thenReturn(
                ResponseEntity.status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(responseBody)
        );

        mockMvc.perform(
                        post("/api/quizzes")
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer test-jwt-token"
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isCreated())
                .andExpect(content().json(responseBody));

        verify(restTemplate).exchange(
                eq("http://localhost:8083/api/quizzes?userId=1"),
                eq(HttpMethod.POST),
                any(),
                eq(String.class)
        );
    }

    @Test
    void addQuestionShouldForwardAuthenticatedUserId() throws Exception {

        mockValidJwt();

        String requestBody = """
                {
                  "questionText": "What does JVM stand for?",
                  "optionA": "Java Virtual Machine",
                  "optionB": "Java Version Manager",
                  "optionC": "Java Visual Machine",
                  "optionD": "Java Variable Method",
                  "correctAnswer": "A"
                }
                """;

        String responseBody = """
                {
                  "id": 1,
                  "questionText": "What does JVM stand for?",
                  "optionA": "Java Virtual Machine",
                  "optionB": "Java Version Manager",
                  "optionC": "Java Visual Machine",
                  "optionD": "Java Variable Method"
                }
                """;

        when(restTemplate.exchange(
                eq("http://localhost:8083/api/quizzes/1/questions?userId=1"),
                eq(HttpMethod.POST),
                any(),
                eq(String.class)
        )).thenReturn(
                ResponseEntity.status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(responseBody)
        );

        mockMvc.perform(
                        post("/api/quizzes/1/questions")
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer test-jwt-token"
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isCreated())
                .andExpect(content().json(responseBody));

        verify(restTemplate).exchange(
                eq("http://localhost:8083/api/quizzes/1/questions?userId=1"),
                eq(HttpMethod.POST),
                any(),
                eq(String.class)
        );
    }

    @Test
    void submitQuizShouldForwardAuthenticatedUserId() throws Exception {

        mockValidJwt();

        String requestBody = """
                {
                  "quizId": 1,
                  "answers": [
                    {
                      "questionId": 1,
                      "selectedAnswer": "A"
                    }
                  ]
                }
                """;

        String responseBody = """
                {
                  "attemptId": 1,
                  "quizId": 1,
                  "score": 1,
                  "totalQuestions": 1,
                  "percentage": 100.0
                }
                """;

        when(restTemplate.exchange(
                eq("http://localhost:8083/api/quiz-submissions?userId=1"),
                eq(HttpMethod.POST),
                any(),
                eq(String.class)
        )).thenReturn(
                ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(responseBody)
        );

        mockMvc.perform(
                        post("/api/quiz-submissions")
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer test-jwt-token"
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isOk())
                .andExpect(content().json(responseBody));

        verify(restTemplate).exchange(
                eq("http://localhost:8083/api/quiz-submissions?userId=1"),
                eq(HttpMethod.POST),
                any(),
                eq(String.class)
        );
    }

    @Test
    void generateAiQuizShouldForwardAuthenticatedUserId() throws Exception {

        mockValidJwt();

        String requestBody = """
                {
                  "topic": "Java Collections",
                  "numberOfQuestions": 3,
                  "difficulty": "medium"
                }
                """;

        String responseBody = """
                {
                  "quizId": 3,
                  "title": "Java Collections Medium Difficulty Quiz",
                  "description": "AI generated quiz",
                  "aiGenerated": true,
                  "questions": []
                }
                """;

        when(restTemplate.exchange(
                eq("http://localhost:8083/api/ai-quizzes/generate?userId=1"),
                eq(HttpMethod.POST),
                any(),
                eq(String.class)
        )).thenReturn(
                ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(responseBody)
        );

        mockMvc.perform(
                        post("/api/ai-quizzes/generate")
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer test-jwt-token"
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isOk())
                .andExpect(content().json(responseBody));

        verify(restTemplate).exchange(
                eq("http://localhost:8083/api/ai-quizzes/generate?userId=1"),
                eq(HttpMethod.POST),
                any(),
                eq(String.class)
        );
    }

    @Test
    void protectedQuizEndpointShouldReturnUnauthorizedWithoutJwt()
            throws Exception {

        mockMvc.perform(
                        get("/api/quizzes")
                )
                .andExpect(status().isUnauthorized());
    }
}