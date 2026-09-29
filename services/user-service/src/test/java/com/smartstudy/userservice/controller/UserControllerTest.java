package com.smartstudy.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartstudy.userservice.dto.LoginRequest;
import com.smartstudy.userservice.dto.RegisterRequest;
import com.smartstudy.userservice.exception.AuthenticationException;
import com.smartstudy.userservice.exception.DuplicateEmailException;
import com.smartstudy.userservice.model.User;
import com.smartstudy.userservice.service.UserService;
import com.smartstudy.userservice.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);

        UserController controller = new UserController(userService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(
                        new com.smartstudy.userservice.exception.GlobalExceptionHandler()
                )
                .build();

        objectMapper = new ObjectMapper();
    }

    @Test
    void loginShouldReturnToken() throws Exception {

        when(userService.login(any(LoginRequest.class)))
                .thenReturn("test-jwt-token");

        LoginRequest request = new LoginRequest();
        request.setEmail("dinesh@example.com");
        request.setPassword("password123");

        mockMvc.perform(
                        post("/api/users/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("test-jwt-token"))
                .andExpect(jsonPath("$.message").value("Login successful"));
    }

    @Test
    void loginShouldReturn401WhenAuthenticationFails() throws Exception {

        when(userService.login(any(LoginRequest.class)))
                .thenThrow(
                        new AuthenticationException("Invalid email or password")
                );

        LoginRequest request = new LoginRequest();
        request.setEmail("dinesh@example.com");
        request.setPassword("wrongPassword");

        mockMvc.perform(
                        post("/api/users/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message")
                        .value("Invalid email or password"));
    }

    @Test
    void registerShouldReturn409WhenEmailAlreadyExists() throws Exception {

        when(userService.register(any(User.class)))
                .thenThrow(
                        new DuplicateEmailException("Email is already registered")
                );

        RegisterRequest request = new RegisterRequest();
        request.setName("Dinesh");
        request.setEmail("dinesh@example.com");
        request.setPassword("password123");

        mockMvc.perform(
                        post("/api/users/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("Email is already registered"));
    }

    @Test
    void profileShouldReturn401WhenAuthorizationHeaderIsInvalid()
            throws Exception {

        mockMvc.perform(
                        get("/api/users/profile")
                                .header("Authorization", "InvalidToken")
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message")
                        .value("Authorization header is missing or invalid"));
    }
}