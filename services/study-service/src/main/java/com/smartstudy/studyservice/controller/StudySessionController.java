package com.smartstudy.studyservice.controller;

import com.smartstudy.studyservice.dto.StudySessionRequest;
import com.smartstudy.studyservice.dto.StudySessionResponse;
import com.smartstudy.studyservice.service.StudySessionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/study-sessions")
public class StudySessionController {

    private final StudySessionService studySessionService;

    public StudySessionController(StudySessionService studySessionService) {
        this.studySessionService = studySessionService;
    }

    @PostMapping("/start")
    public ResponseEntity<StudySessionResponse> startSession(
            @RequestParam Long userId,
            @Valid @RequestBody StudySessionRequest request) {

        StudySessionResponse session =
                studySessionService.startSession(userId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(session);
    }

    @PostMapping("/stop")
    public ResponseEntity<StudySessionResponse> stopSession(
            @RequestParam Long userId) {

        StudySessionResponse session =
                studySessionService.stopSession(userId);

        return ResponseEntity.ok(session);
    }

    @GetMapping("/history")
    public ResponseEntity<List<StudySessionResponse>> getHistory(
            @RequestParam Long userId) {

        List<StudySessionResponse> history =
                studySessionService.getHistory(userId);

        return ResponseEntity.ok(history);
    }
}