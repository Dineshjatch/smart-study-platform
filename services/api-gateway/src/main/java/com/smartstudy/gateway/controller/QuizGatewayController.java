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
public class QuizGatewayController {

    private final RestTemplate restTemplate;
    private final String quizServiceUrl;

    public QuizGatewayController(
            RestTemplate restTemplate,
            @Value("${quiz-service.url}") String quizServiceUrl
    ) {
        this.restTemplate = restTemplate;
        this.quizServiceUrl = quizServiceUrl;
    }

    // ----------------------------
    // Quiz endpoints
    // ----------------------------

    @PostMapping("/api/quizzes")
    public ResponseEntity<String> createQuiz(
            HttpServletRequest request,
            @RequestBody String body
    ) {
        Long userId = getAuthenticatedUserId(request);

        return forwardPost(
                "/api/quizzes?userId=" + userId,
                body
        );
    }

    @GetMapping("/api/quizzes")
    public ResponseEntity<String> getQuizzes(
            HttpServletRequest request
    ) {
        Long userId = getAuthenticatedUserId(request);

        return forwardGet(
                "/api/quizzes?userId=" + userId,
                request
        );
    }

    @GetMapping("/api/quizzes/{quizId}")
    public ResponseEntity<String> getQuiz(
            HttpServletRequest request,
            @PathVariable Long quizId
    ) {
        Long userId = getAuthenticatedUserId(request);

        return forwardGet(
                "/api/quizzes/" + quizId + "?userId=" + userId,
                request
        );
    }

    @PutMapping("/api/quizzes/{quizId}")
    public ResponseEntity<String> updateQuiz(
            HttpServletRequest request,
            @PathVariable Long quizId,
            @RequestBody String body
    ) {
        Long userId = getAuthenticatedUserId(request);

        return forwardPut(
                "/api/quizzes/" + quizId + "?userId=" + userId,
                body
        );
    }

    @DeleteMapping("/api/quizzes/{quizId}")
    public ResponseEntity<String> deleteQuiz(
            HttpServletRequest request,
            @PathVariable Long quizId
    ) {
        Long userId = getAuthenticatedUserId(request);

        return forwardDelete(
                "/api/quizzes/" + quizId + "?userId=" + userId,
                request
        );
    }

    // ----------------------------
    // Question endpoints
    // ----------------------------

    @PostMapping("/api/quizzes/{quizId}/questions")
    public ResponseEntity<String> addQuestion(
            HttpServletRequest request,
            @PathVariable Long quizId,
            @RequestBody String body
    ) {
        Long userId = getAuthenticatedUserId(request);

        return forwardPost(
                "/api/quizzes/" + quizId
                        + "/questions?userId=" + userId,
                body
        );
    }

    @GetMapping("/api/quizzes/{quizId}/questions")
    public ResponseEntity<String> getQuestions(
            HttpServletRequest request,
            @PathVariable Long quizId
    ) {
        Long userId = getAuthenticatedUserId(request);

        return forwardGet(
                "/api/quizzes/" + quizId
                        + "/questions?userId=" + userId,
                request
        );
    }

    // ----------------------------
    // AI quiz generation
    // ----------------------------

    @PostMapping("/api/ai-quizzes/generate")
    public ResponseEntity<String> generateAiQuiz(
            HttpServletRequest request,
            @RequestBody String body
    ) {
        Long userId = getAuthenticatedUserId(request);

        return forwardPost(
                "/api/ai-quizzes/generate?userId=" + userId,
                body
        );
    }

    // ----------------------------
    // Quiz submission
    // ----------------------------

    @PostMapping("/api/quiz-submissions")
    public ResponseEntity<String> submitQuiz(
            HttpServletRequest request,
            @RequestBody String body
    ) {
        Long userId = getAuthenticatedUserId(request);

        return forwardPost(
                "/api/quiz-submissions?userId=" + userId,
                body
        );
    }

    // ----------------------------
    // Quiz attempt history
    // ----------------------------

    @GetMapping("/api/quiz-attempts")
    public ResponseEntity<String> getUserAttempts(
            HttpServletRequest request
    ) {
        Long userId = getAuthenticatedUserId(request);

        return forwardGet(
                "/api/quiz-attempts?userId=" + userId,
                request
        );
    }

    @GetMapping("/api/quiz-attempts/quiz/{quizId}")
    public ResponseEntity<String> getQuizAttempts(
            HttpServletRequest request,
            @PathVariable Long quizId
    ) {
        Long userId = getAuthenticatedUserId(request);

        return forwardGet(
                "/api/quiz-attempts/quiz/"
                        + quizId
                        + "?userId="
                        + userId,
                request
        );
    }

    // ----------------------------
    // HTTP forwarding helpers
    // ----------------------------

    private ResponseEntity<String> forwardPost(
            String path,
            String body
    ) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity =
                new HttpEntity<>(body, headers);

        try {
            return restTemplate.exchange(
                    quizServiceUrl + path,
                    HttpMethod.POST,
                    entity,
                    String.class
            );
        } catch (RestClientResponseException e) {
            return downstreamError(e);
        }
    }

    private ResponseEntity<String> forwardPut(
            String path,
            String body
    ) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity =
                new HttpEntity<>(body, headers);

        try {
            return restTemplate.exchange(
                    quizServiceUrl + path,
                    HttpMethod.PUT,
                    entity,
                    String.class
            );
        } catch (RestClientResponseException e) {
            return downstreamError(e);
        }
    }

    private ResponseEntity<String> forwardGet(
            String path,
            HttpServletRequest request
    ) {
        HttpHeaders headers = copyAuthorizationHeader(request);

        HttpEntity<Void> entity =
                new HttpEntity<>(headers);

        try {
            return restTemplate.exchange(
                    quizServiceUrl + path,
                    HttpMethod.GET,
                    entity,
                    String.class
            );
        } catch (RestClientResponseException e) {
            return downstreamError(e);
        }
    }

    private ResponseEntity<String> forwardDelete(
            String path,
            HttpServletRequest request
    ) {
        HttpHeaders headers = copyAuthorizationHeader(request);

        HttpEntity<Void> entity =
                new HttpEntity<>(headers);

        try {
            return restTemplate.exchange(
                    quizServiceUrl + path,
                    HttpMethod.DELETE,
                    entity,
                    String.class
            );
        } catch (RestClientResponseException e) {
            return downstreamError(e);
        }
    }

    private HttpHeaders copyAuthorizationHeader(
            HttpServletRequest request
    ) {
        HttpHeaders headers = new HttpHeaders();

        String authorization =
                request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorization != null
                && !authorization.isBlank()) {
            headers.set(
                    HttpHeaders.AUTHORIZATION,
                    authorization
            );
        }

        return headers;
    }

    private Long getAuthenticatedUserId(
            HttpServletRequest request
    ) {
        Object userId =
                request.getAttribute("authenticatedUserId");

        if (userId == null) {
            throw new IllegalStateException(
                    "Authenticated user ID is missing"
            );
        }

        return (Long) userId;
    }

    private ResponseEntity<String> downstreamError(
            RestClientResponseException e
    ) {
        return ResponseEntity
                .status(e.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(e.getResponseBodyAsString());
    }
}