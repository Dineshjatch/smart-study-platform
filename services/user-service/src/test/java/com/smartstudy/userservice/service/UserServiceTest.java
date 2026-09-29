package com.smartstudy.userservice.service;

import com.smartstudy.userservice.dto.LoginRequest;
import com.smartstudy.userservice.dto.UserProfileResponse;
import com.smartstudy.userservice.model.User;
import com.smartstudy.userservice.repository.UserRepository;
import com.smartstudy.userservice.exception.AuthenticationException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Test
    void registerShouldHashPasswordAndSaveUser() {

        UserRepository userRepository = mock(UserRepository.class);
        JwtService jwtService = mock(JwtService.class);

        when(userRepository.findByEmail("dinesh@example.com"))
                .thenReturn(Optional.empty());

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserService userService = new UserService(userRepository, jwtService);

        User user = new User(
                "Dinesh",
                "dinesh@example.com",
                "myPassword123"
        );

        User savedUser = userService.register(user);

        assertNotEquals("myPassword123", savedUser.getPassword());
        assertTrue(savedUser.getPassword().startsWith("$2"));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(userCaptor.capture());

        User userSentToRepository = userCaptor.getValue();

        assertEquals("Dinesh", userSentToRepository.getName());
        assertEquals("dinesh@example.com", userSentToRepository.getEmail());
        assertNotEquals("myPassword123", userSentToRepository.getPassword());
    }

    @Test
    void loginShouldReturnJwtWhenCredentialsAreCorrect() {

        UserRepository userRepository = mock(UserRepository.class);
        JwtService jwtService = mock(JwtService.class);

        org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder =
                new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();

        User user = new User(
                "Dinesh",
                "dinesh@example.com",
                "myPassword123"
        );

        user.setPassword(encoder.encode("myPassword123"));

        when(userRepository.findByEmail("dinesh@example.com"))
                .thenReturn(Optional.of(user));

        when(jwtService.generateToken(user.getId(), user.getEmail()))
                .thenReturn("test-jwt-token");

        UserService userService = new UserService(userRepository, jwtService);

        LoginRequest request = new LoginRequest();
        request.setEmail("dinesh@example.com");
        request.setPassword("myPassword123");

        String token = userService.login(request);

        assertEquals("test-jwt-token", token);

        verify(userRepository).findByEmail("dinesh@example.com");
        verify(jwtService).generateToken(user.getId(), user.getEmail());
    }

    @Test
    void loginShouldRejectWrongPassword() {

        UserRepository userRepository = mock(UserRepository.class);
        JwtService jwtService = mock(JwtService.class);

        org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder =
                new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();

        User user = new User(
                "Dinesh",
                "dinesh@example.com",
                encoder.encode("correctPassword")
        );

        when(userRepository.findByEmail("dinesh@example.com"))
                .thenReturn(Optional.of(user));

        UserService userService = new UserService(userRepository, jwtService);

        LoginRequest request = new LoginRequest();
        request.setEmail("dinesh@example.com");
        request.setPassword("wrongPassword");

        AuthenticationException exception = assertThrows(
                AuthenticationException.class,
                () -> userService.login(request)
        );

        assertEquals("Invalid email or password", exception.getMessage());

        // A JWT must never be generated after authentication fails.
        verify(jwtService, never()).generateToken(anyLong(), anyString());
    }

    @Test
    void getProfileShouldReturnUserProfileForValidToken() {

        UserRepository userRepository = mock(UserRepository.class);
        JwtService jwtService = mock(JwtService.class);

        User user = new User(
                "Dinesh",
                "dinesh@example.com",
                "hashed-password"
        );

        when(jwtService.isTokenValid("valid-token"))
                .thenReturn(true);

        when(jwtService.extractEmail("valid-token"))
                .thenReturn("dinesh@example.com");

        when(userRepository.findByEmail("dinesh@example.com"))
                .thenReturn(Optional.of(user));

        UserService userService = new UserService(userRepository, jwtService);

        UserProfileResponse profile = userService.getProfile("valid-token");

        assertEquals(user.getId(), profile.getId());
        assertEquals("Dinesh", profile.getName());
        assertEquals("dinesh@example.com", profile.getEmail());

        verify(jwtService).isTokenValid("valid-token");
        verify(jwtService).extractEmail("valid-token");
        verify(userRepository).findByEmail("dinesh@example.com");
    }

    @Test
    void getProfileShouldRejectInvalidToken() {

        UserRepository userRepository = mock(UserRepository.class);
        JwtService jwtService = mock(JwtService.class);

        when(jwtService.isTokenValid("invalid-token"))
                .thenReturn(false);

        UserService userService = new UserService(userRepository, jwtService);

        AuthenticationException exception = assertThrows(
                AuthenticationException.class,
                () -> userService.getProfile("invalid-token")
        );

        assertEquals("Invalid or expired token", exception.getMessage());

        // An invalid token must never cause a database lookup.
        verify(userRepository, never()).findByEmail(anyString());

        // We should not try to extract information from an invalid token.
        verify(jwtService, never()).extractEmail(anyString());
    }
}