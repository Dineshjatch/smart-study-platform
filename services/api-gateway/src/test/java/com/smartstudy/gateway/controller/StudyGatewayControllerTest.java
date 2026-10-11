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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StudyGatewayController.class)
@TestPropertySource(properties = {
        "study-service.url=http://localhost:8082"
})
class StudyGatewayControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RestTemplate restTemplate;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void createSubjectShouldForwardAuthenticatedUserId() throws Exception {

        String authorizationHeader = "Bearer test-jwt-token";

        String requestBody = """
                {
                  "name": "Java",
                  "description": "Java fundamentals"
                }
                """;

        String responseBody = """
                {
                  "id": 1,
                  "name": "Java",
                  "description": "Java fundamentals"
                }
                """;

        when(jwtService.isTokenValid("test-jwt-token"))
                .thenReturn(true);

        when(jwtService.extractUserId("test-jwt-token"))
                .thenReturn(1L);

        when(restTemplate.exchange(
                eq("http://localhost:8082/api/subjects?userId=1"),
                eq(HttpMethod.POST),
                any(),
                eq(String.class)
        )).thenReturn(
                ResponseEntity.status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(responseBody)
        );

        mockMvc.perform(
                        post("/api/subjects")
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        authorizationHeader
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isCreated())
                .andExpect(content().json(responseBody));

        verify(jwtService)
                .isTokenValid("test-jwt-token");

        verify(jwtService)
                .extractUserId("test-jwt-token");

        verify(restTemplate).exchange(
                eq("http://localhost:8082/api/subjects?userId=1"),
                eq(HttpMethod.POST),
                any(),
                eq(String.class)
        );
    }

    @Test
    void getStudyHistoryShouldForwardAuthenticatedUserId() throws Exception {

        String authorizationHeader = "Bearer test-jwt-token";

        String responseBody = """
                [
                  {
                    "id": 1,
                    "subjectId": 10,
                    "durationSeconds": 3600
                  }
                ]
                """;

        when(jwtService.isTokenValid("test-jwt-token"))
                .thenReturn(true);

        when(jwtService.extractUserId("test-jwt-token"))
                .thenReturn(1L);

        when(restTemplate.exchange(
                eq("http://localhost:8082/api/study-sessions/history?userId=1"),
                eq(HttpMethod.GET),
                any(),
                eq(String.class)
        )).thenReturn(
                ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(responseBody)
        );

        mockMvc.perform(
                        get("/api/study-sessions/history")
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        authorizationHeader
                                )
                )
                .andExpect(status().isOk())
                .andExpect(content().json(responseBody));

        verify(restTemplate).exchange(
                eq("http://localhost:8082/api/study-sessions/history?userId=1"),
                eq(HttpMethod.GET),
                any(),
                eq(String.class)
        );
    }

    @Test
    void startStudySessionShouldForwardAuthenticatedUserId() throws Exception {

        String authorizationHeader = "Bearer test-jwt-token";

        String requestBody = """
                {
                  "subjectId": 10
                }
                """;

        String responseBody = """
                {
                  "id": 50,
                  "subjectId": 10
                }
                """;

        when(jwtService.isTokenValid("test-jwt-token"))
                .thenReturn(true);

        when(jwtService.extractUserId("test-jwt-token"))
                .thenReturn(1L);

        when(restTemplate.exchange(
                eq("http://localhost:8082/api/study-sessions/start?userId=1"),
                eq(HttpMethod.POST),
                any(),
                eq(String.class)
        )).thenReturn(
                ResponseEntity.status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(responseBody)
        );

        mockMvc.perform(
                        post("/api/study-sessions/start")
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        authorizationHeader
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isCreated())
                .andExpect(content().json(responseBody));

        verify(restTemplate).exchange(
                eq("http://localhost:8082/api/study-sessions/start?userId=1"),
                eq(HttpMethod.POST),
                any(),
                eq(String.class)
        );
    }

    @Test
    void deleteSubjectShouldForwardAuthenticatedUserId() throws Exception {

        String authorizationHeader = "Bearer test-jwt-token";

        when(jwtService.isTokenValid("test-jwt-token"))
                .thenReturn(true);

        when(jwtService.extractUserId("test-jwt-token"))
                .thenReturn(1L);

        when(restTemplate.exchange(
                eq("http://localhost:8082/api/subjects/10?userId=1"),
                eq(HttpMethod.DELETE),
                any(),
                eq(String.class)
        )).thenReturn(
                ResponseEntity.noContent().build()
        );

        mockMvc.perform(
                        delete("/api/subjects/10")
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        authorizationHeader
                                )
                )
                .andExpect(status().isNoContent());

        verify(restTemplate).exchange(
                eq("http://localhost:8082/api/subjects/10?userId=1"),
                eq(HttpMethod.DELETE),
                any(),
                eq(String.class)
        );
    }

    @Test
    void protectedStudyEndpointShouldReturnUnauthorizedWithoutJwt() throws Exception {

        mockMvc.perform(
                        get("/api/study-sessions/history")
                )
                .andExpect(status().isUnauthorized());

        verify(jwtService, org.mockito.Mockito.never())
                .isTokenValid(any());
    }
}