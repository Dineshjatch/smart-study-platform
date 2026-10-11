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
public class StudyGatewayController {

    private final RestTemplate restTemplate;
    private final String studyServiceUrl;

    public StudyGatewayController(
            RestTemplate restTemplate,
            @Value("${study-service.url}") String studyServiceUrl
    ) {
        this.restTemplate = restTemplate;
        this.studyServiceUrl = studyServiceUrl;
    }

    // ----------------------------
    // Subject endpoints
    // ----------------------------

    @PostMapping("/api/subjects")
    public ResponseEntity<String> createSubject(
            HttpServletRequest request,
            @RequestBody String body
    ) {
        Long userId = getAuthenticatedUserId(request);

        return forwardPost(
                "/api/subjects?userId=" + userId,
                body
        );
    }

    @GetMapping("/api/subjects")
    public ResponseEntity<String> getSubjects(
            HttpServletRequest request
    ) {
        Long userId = getAuthenticatedUserId(request);

        return forwardGet(
                "/api/subjects?userId=" + userId,
                request
        );
    }

    @GetMapping("/api/subjects/{subjectId}")
    public ResponseEntity<String> getSubject(
            HttpServletRequest request,
            @PathVariable Long subjectId
    ) {
        Long userId = getAuthenticatedUserId(request);

        return forwardGet(
                "/api/subjects/" + subjectId + "?userId=" + userId,
                request
        );
    }

    @PutMapping("/api/subjects/{subjectId}")
    public ResponseEntity<String> updateSubject(
            HttpServletRequest request,
            @PathVariable Long subjectId,
            @RequestBody String body
    ) {
        Long userId = getAuthenticatedUserId(request);

        return forwardPut(
                "/api/subjects/" + subjectId + "?userId=" + userId,
                body
        );
    }

    @DeleteMapping("/api/subjects/{subjectId}")
    public ResponseEntity<String> deleteSubject(
            HttpServletRequest request,
            @PathVariable Long subjectId
    ) {
        Long userId = getAuthenticatedUserId(request);

        return forwardDelete(
                "/api/subjects/" + subjectId + "?userId=" + userId,
                request
        );
    }

    // ----------------------------
    // Study session endpoints
    // ----------------------------

    @PostMapping("/api/study-sessions/start")
    public ResponseEntity<String> startStudySession(
            HttpServletRequest request,
            @RequestBody String body
    ) {
        Long userId = getAuthenticatedUserId(request);

        return forwardPost(
                "/api/study-sessions/start?userId=" + userId,
                body
        );
    }

    @PostMapping("/api/study-sessions/stop")
    public ResponseEntity<String> stopStudySession(
            HttpServletRequest request
    ) {
        Long userId = getAuthenticatedUserId(request);

        return forwardPost(
                "/api/study-sessions/stop?userId=" + userId,
                "{}"
        );
    }

    @GetMapping("/api/study-sessions/history")
    public ResponseEntity<String> getStudyHistory(
            HttpServletRequest request
    ) {
        Long userId = getAuthenticatedUserId(request);

        return forwardGet(
                "/api/study-sessions/history?userId=" + userId,
                request
        );
    }

    // ----------------------------
    // Study statistics
    // ----------------------------

    @GetMapping("/api/study-stats")
    public ResponseEntity<String> getStudyStats(
            HttpServletRequest request
    ) {
        Long userId = getAuthenticatedUserId(request);

        return forwardGet(
                "/api/study-stats?userId=" + userId,
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
                    studyServiceUrl + path,
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
                    studyServiceUrl + path,
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
                    studyServiceUrl + path,
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
                    studyServiceUrl + path,
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