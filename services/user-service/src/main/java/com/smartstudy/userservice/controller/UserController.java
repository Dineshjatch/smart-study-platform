package com.smartstudy.userservice.controller;

import com.smartstudy.userservice.dto.LoginRequest;
import com.smartstudy.userservice.dto.RegisterRequest;
import com.smartstudy.userservice.dto.UserProfileResponse;
import com.smartstudy.userservice.exception.AuthenticationException;
import com.smartstudy.userservice.model.User;
import com.smartstudy.userservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(
            @Valid @RequestBody RegisterRequest request) {

        User user = new User(
                request.getName(),
                request.getEmail(),
                request.getPassword()
        );

        User savedUser = userService.register(user);

        // Never send the password or password hash back to the client.
        Map<String, Object> response = Map.of(
                "id", savedUser.getId(),
                "name", savedUser.getName(),
                "email", savedUser.getEmail(),
                "message", "User registered successfully"
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(
            @Valid @RequestBody LoginRequest request) {

        String token = userService.login(request);

        Map<String, String> response = Map.of(
                "token", token,
                "message", "Login successful"
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getProfile(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {

        String token = extractBearerToken(authorizationHeader);

        UserProfileResponse profile = userService.getProfile(token);

        return ResponseEntity.ok(profile);
    }

    private String extractBearerToken(String authorizationHeader) {

        if (authorizationHeader == null
                || !authorizationHeader.startsWith("Bearer ")
                || authorizationHeader.length() <= 7) {

            throw new AuthenticationException(
                    "Authorization header is missing or invalid"
            );
        }

        return authorizationHeader.substring(7);
    }
}