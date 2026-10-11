package com.smartstudy.quizservice.controller;

import com.smartstudy.quizservice.dto.QuizAttemptResponse;
import com.smartstudy.quizservice.service.QuizAttemptService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quiz-attempts")
public class QuizAttemptController {

    private final QuizAttemptService quizAttemptService;

    public QuizAttemptController(QuizAttemptService quizAttemptService) {
        this.quizAttemptService = quizAttemptService;
    }

    @GetMapping
    public ResponseEntity<List<QuizAttemptResponse>> getUserAttempts(
            @RequestParam Long userId
    ) {
        return ResponseEntity.ok(
                quizAttemptService.getUserAttempts(userId)
        );
    }

    @GetMapping("/quiz/{quizId}")
    public ResponseEntity<List<QuizAttemptResponse>> getQuizAttempts(
            @RequestParam Long userId,
            @PathVariable Long quizId
    ) {
        return ResponseEntity.ok(
                quizAttemptService.getQuizAttempts(userId, quizId)
        );
    }
}