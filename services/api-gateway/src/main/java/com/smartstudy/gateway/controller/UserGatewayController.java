package com.smartstudy.gateway.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/users")
public class UserGatewayController {

    private final RestTemplate restTemplate;
    private final String userServiceUrl;

    public UserGatewayController(
            RestTemplate restTemplate,
            @Value("${user-service.url}") String userServiceUrl
    ) {
        this.restTemplate = restTemplate;
        this.userServiceUrl = userServiceUrl;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(
            @RequestBody String body
    ) {
        return forwardPost(
                "/api/users/register",
                body,
                null
        );
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(
            @RequestBody String body
    ) {
        return forwardPost(
                "/api/users/login",
                body,
                null
        );
    }

    @GetMapping("/profile")
    public ResponseEntity<String> profile(
            HttpServletRequest request
    ) {
        String authorizationHeader =
                request.getHeader(HttpHeaders.AUTHORIZATION);

        return forwardGet(
                "/api/users/profile",
                authorizationHeader
        );
    }

    private ResponseEntity<String> forwardPost(
            String path,
            String body,
            String authorizationHeader
    ) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        if (authorizationHeader != null
                && !authorizationHeader.isBlank()) {
            headers.set(HttpHeaders.AUTHORIZATION, authorizationHeader);
        }

        HttpEntity<String> requestEntity =
                new HttpEntity<>(body, headers);

        return executePost(path, requestEntity);
    }

    private ResponseEntity<String> forwardGet(
            String path,
            String authorizationHeader
    ) {

        HttpHeaders headers = new HttpHeaders();

        if (authorizationHeader != null
                && !authorizationHeader.isBlank()) {
            headers.set(HttpHeaders.AUTHORIZATION, authorizationHeader);
        }

        HttpEntity<Void> requestEntity =
                new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    userServiceUrl + path,
                    HttpMethod.GET,
                    requestEntity,
                    String.class
            );

            return buildJsonResponse(response);
        } catch (RestClientResponseException e) {
            return ResponseEntity
                    .status(e.getStatusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(e.getResponseBodyAsString());
        }
    }

    private ResponseEntity<String> executePost(
            String path,
            HttpEntity<String> requestEntity
    ) {

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    userServiceUrl + path,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            return buildJsonResponse(response);
        } catch (RestClientResponseException e) {
            return ResponseEntity
                    .status(e.getStatusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(e.getResponseBodyAsString());
        }
    }

    private ResponseEntity<String> buildJsonResponse(
            ResponseEntity<String> response
    ) {
        return ResponseEntity
                .status(response.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(response.getBody());
    }
}