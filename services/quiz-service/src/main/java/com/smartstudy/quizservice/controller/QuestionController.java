package com.smartstudy.quizservice.controller;

import com.smartstudy.quizservice.dto.QuestionRequest;
import com.smartstudy.quizservice.dto.QuestionResponse;
import com.smartstudy.quizservice.service.QuestionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quizzes/{quizId}/questions")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @PostMapping
    public ResponseEntity<QuestionResponse> addQuestion(
            @RequestParam Long userId,
            @PathVariable Long quizId,
            @Valid @RequestBody QuestionRequest request) {

        QuestionResponse question =
                questionService.addQuestion(
                        userId,
                        quizId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(question);
    }

    @GetMapping
    public ResponseEntity<List<QuestionResponse>> getQuestions(
            @RequestParam Long userId,
            @PathVariable Long quizId) {

        List<QuestionResponse> questions =
                questionService.getQuestions(
                        userId,
                        quizId
                );

        return ResponseEntity.ok(questions);
    }
}