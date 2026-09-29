package com.smartstudy.studyservice.service;

import com.smartstudy.studyservice.dto.StudyStatsResponse;
import com.smartstudy.studyservice.model.StudySession;
import com.smartstudy.studyservice.repository.StudySessionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudyStatsService {

    private final StudySessionRepository studySessionRepository;

    public StudyStatsService(StudySessionRepository studySessionRepository) {
        this.studySessionRepository = studySessionRepository;
    }

    public StudyStatsResponse getStats(Long userId) {

        List<StudySession> sessions =
                studySessionRepository
                        .findByUserIdOrderByStartedAtDesc(userId);

        long totalStudySeconds = 0;
        long totalSessions = 0;

        for (StudySession session : sessions) {

            // Active sessions have no duration yet, so ignore them.
            if (session.getDurationSeconds() != null) {
                totalStudySeconds += session.getDurationSeconds();
                totalSessions++;
            }
        }

        double averageSessionSeconds = 0.0;

        if (totalSessions > 0) {
            averageSessionSeconds =
                    (double) totalStudySeconds / totalSessions;
        }

        return new StudyStatsResponse(
                totalStudySeconds,
                totalSessions,
                averageSessionSeconds
        );
    }
}