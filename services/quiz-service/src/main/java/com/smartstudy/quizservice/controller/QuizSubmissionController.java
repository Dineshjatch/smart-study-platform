package com.smartstudy.quizservice.controller;

import com.smartstudy.quizservice.dto.QuizResultResponse;
import com.smartstudy.quizservice.dto.QuizSubmissionRequest;
import com.smartstudy.quizservice.service.QuizSubmissionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quiz-submissions")
public class QuizSubmissionController {

    private final QuizSubmissionService quizSubmissionService;

    public QuizSubmissionController(
            QuizSubmissionService quizSubmissionService) {

        this.quizSubmissionService = quizSubmissionService;
    }

    @PostMapping
    public ResponseEntity<QuizResultResponse> submitQuiz(
            @RequestParam Long userId,
            @Valid @RequestBody QuizSubmissionRequest request) {

        QuizResultResponse result =
                quizSubmissionService.submitQuiz(
                        userId,
                        request
                );

        return ResponseEntity.ok(result);
    }
}