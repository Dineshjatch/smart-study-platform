package com.smartstudy.studyservice.controller;

import com.smartstudy.studyservice.dto.SubjectRequest;
import com.smartstudy.studyservice.dto.SubjectResponse;
import com.smartstudy.studyservice.service.SubjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subjects")
public class SubjectController {

    private final SubjectService subjectService;

    public SubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @PostMapping
    public ResponseEntity<SubjectResponse> createSubject(
            @RequestParam Long userId,
            @Valid @RequestBody SubjectRequest request) {

        SubjectResponse subject =
                subjectService.createSubject(userId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(subject);
    }

    @GetMapping
    public ResponseEntity<List<SubjectResponse>> getSubjects(
            @RequestParam Long userId) {

        List<SubjectResponse> subjects =
                subjectService.getSubjectsByUser(userId);

        return ResponseEntity.ok(subjects);
    }

    @GetMapping("/{subjectId}")
    public ResponseEntity<SubjectResponse> getSubject(
            @RequestParam Long userId,
            @PathVariable Long subjectId) {

        SubjectResponse subject =
                subjectService.getSubject(userId, subjectId);

        return ResponseEntity.ok(subject);
    }

    @PutMapping("/{subjectId}")
    public ResponseEntity<SubjectResponse> updateSubject(
            @RequestParam Long userId,
            @PathVariable Long subjectId,
            @Valid @RequestBody SubjectRequest request) {

        SubjectResponse subject =
                subjectService.updateSubject(
                        userId,
                        subjectId,
                        request
                );

        return ResponseEntity.ok(subject);
    }

    @DeleteMapping("/{subjectId}")
    public ResponseEntity<Void> deleteSubject(
            @RequestParam Long userId,
            @PathVariable Long subjectId) {

        subjectService.deleteSubject(userId, subjectId);

        return ResponseEntity.noContent().build();
    }
}