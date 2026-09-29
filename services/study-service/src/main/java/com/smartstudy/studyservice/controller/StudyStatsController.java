package com.smartstudy.studyservice.controller;

import com.smartstudy.studyservice.dto.StudyStatsResponse;
import com.smartstudy.studyservice.service.StudyStatsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/study-stats")
public class StudyStatsController {

    private final StudyStatsService studyStatsService;

    public StudyStatsController(StudyStatsService studyStatsService) {
        this.studyStatsService = studyStatsService;
    }

    @GetMapping
    public ResponseEntity<StudyStatsResponse> getStats(
            @RequestParam Long userId) {

        StudyStatsResponse stats =
                studyStatsService.getStats(userId);

        return ResponseEntity.ok(stats);
    }
}