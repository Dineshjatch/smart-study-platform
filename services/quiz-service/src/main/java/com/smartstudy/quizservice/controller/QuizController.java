package com.smartstudy.quizservice.controller;

import com.smartstudy.quizservice.dto.QuizRequest;
import com.smartstudy.quizservice.dto.QuizResponse;
import com.smartstudy.quizservice.service.QuizService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quizzes")
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @PostMapping
    public ResponseEntity<QuizResponse> createQuiz(
            @RequestParam Long userId,
            @Valid @RequestBody QuizRequest request) {

        QuizResponse quiz =
                quizService.createQuiz(userId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(quiz);
    }

    @GetMapping
    public ResponseEntity<List<QuizResponse>> getQuizzes(
            @RequestParam Long userId) {

        List<QuizResponse> quizzes =
                quizService.getQuizzesByUser(userId);

        return ResponseEntity.ok(quizzes);
    }

    @GetMapping("/{quizId}")
    public ResponseEntity<QuizResponse> getQuiz(
            @RequestParam Long userId,
            @PathVariable Long quizId) {

        QuizResponse quiz =
                quizService.getQuiz(userId, quizId);

        return ResponseEntity.ok(quiz);
    }

    @PutMapping("/{quizId}")
    public ResponseEntity<QuizResponse> updateQuiz(
            @RequestParam Long userId,
            @PathVariable Long quizId,
            @Valid @RequestBody QuizRequest request) {

        QuizResponse quiz =
                quizService.updateQuiz(
                        userId,
                        quizId,
                        request
                );

        return ResponseEntity.ok(quiz);
    }

    @DeleteMapping("/{quizId}")
    public ResponseEntity<Void> deleteQuiz(
            @RequestParam Long userId,
            @PathVariable Long quizId) {

        quizService.deleteQuiz(userId, quizId);

        return ResponseEntity.noContent().build();
    }
}