package com.smartstudy.gateway.controller;

import com.smartstudy.gateway.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserGatewayController.class)
@TestPropertySource(properties = {
        "user-service.url=http://localhost:8081"
})
class UserGatewayControllerTest {

    @MockitoBean
    private JwtService jwtService;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RestTemplate restTemplate;

    @Test
    void registerShouldForwardRequestToUserService() throws Exception {

        String requestBody = """
                {
                  "name": "Dinesh",
                  "email": "dinesh@example.com",
                  "password": "password123"
                }
                """;

        String responseBody = """
                {
                  "id": 1,
                  "name": "Dinesh",
                  "email": "dinesh@example.com",
                  "message": "Registration successful"
                }
                """;

        when(restTemplate.exchange(
                eq("http://localhost:8081/api/users/register"),
                eq(org.springframework.http.HttpMethod.POST),
                any(),
                eq(String.class)
        )).thenReturn(
                ResponseEntity.status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(responseBody)
        );

        mockMvc.perform(
                        post("/api/users/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isCreated())
                .andExpect(content().json(responseBody));

        verify(restTemplate).exchange(
                eq("http://localhost:8081/api/users/register"),
                eq(org.springframework.http.HttpMethod.POST),
                any(),
                eq(String.class)
        );
    }

    @Test
    void loginShouldForwardRequestToUserService() throws Exception {

        String requestBody = """
                {
                  "email": "dinesh@example.com",
                  "password": "password123"
                }
                """;

        String responseBody = """
                {
                  "token": "test-jwt-token",
                  "message": "Login successful"
                }
                """;

        when(restTemplate.exchange(
                eq("http://localhost:8081/api/users/login"),
                eq(org.springframework.http.HttpMethod.POST),
                any(),
                eq(String.class)
        )).thenReturn(
                ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(responseBody)
        );

        mockMvc.perform(
                        post("/api/users/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isOk())
                .andExpect(content().json(responseBody));

        verify(restTemplate).exchange(
                eq("http://localhost:8081/api/users/login"),
                eq(org.springframework.http.HttpMethod.POST),
                any(),
                eq(String.class)
        );
    }

    @Test
    void profileShouldForwardAuthorizationHeader() throws Exception {

        String responseBody = """
                {
                  "id": 1,
                  "name": "Dinesh",
                  "email": "dinesh@example.com"
                }
                """;

        String authorizationHeader = "Bearer test-jwt-token";

        when(jwtService.isTokenValid("test-jwt-token"))
                .thenReturn(true);

        when(jwtService.extractUserId("test-jwt-token"))
                .thenReturn(1L);

        when(restTemplate.exchange(
                eq("http://localhost:8081/api/users/profile"),
                eq(org.springframework.http.HttpMethod.GET),
                any(),
                eq(String.class)
        )).thenReturn(
                ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(responseBody)
        );

        mockMvc.perform(
                        get("/api/users/profile")
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        authorizationHeader
                                )
                )
                .andExpect(status().isOk())
                .andExpect(content().json(responseBody));

        verify(restTemplate).exchange(
                eq("http://localhost:8081/api/users/profile"),
                eq(org.springframework.http.HttpMethod.GET),
                any(),
                eq(String.class)
        );
    }

    @Test
    void registerShouldPreserveDownstreamErrorStatus() throws Exception {

        String requestBody = """
                {
                  "name": "Dinesh",
                  "email": "dinesh@example.com",
                  "password": "password123"
                }
                """;

        String errorBody = """
                {
                  "message": "Email is already registered"
                }
                """;

        when(restTemplate.exchange(
                eq("http://localhost:8081/api/users/register"),
                eq(org.springframework.http.HttpMethod.POST),
                any(),
                eq(String.class)
        )).thenReturn(
                ResponseEntity.status(HttpStatus.CONFLICT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(errorBody)
        );

        mockMvc.perform(
                        post("/api/users/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isConflict())
                .andExpect(content().json(errorBody));
    }
}