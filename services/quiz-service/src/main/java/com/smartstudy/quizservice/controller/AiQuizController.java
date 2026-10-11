package com.smartstudy.quizservice.controller;

import com.smartstudy.quizservice.dto.AiQuizGenerateRequest;
import com.smartstudy.quizservice.dto.AiQuizGenerateResponse;
import com.smartstudy.quizservice.service.AiQuizService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai-quizzes")
public class AiQuizController {

    private final AiQuizService aiQuizService;

    public AiQuizController(AiQuizService aiQuizService) {
        this.aiQuizService = aiQuizService;
    }

    @PostMapping("/generate")
    public ResponseEntity<AiQuizGenerateResponse> generateQuiz(
            @RequestParam Long userId,
            @Valid @RequestBody AiQuizGenerateRequest request
    ) {
        AiQuizGenerateResponse response =
                aiQuizService.generateQuiz(userId, request);

        return ResponseEntity.ok(response);
    }
}