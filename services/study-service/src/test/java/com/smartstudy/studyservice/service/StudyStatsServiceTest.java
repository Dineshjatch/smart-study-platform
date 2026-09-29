package com.smartstudy.studyservice.service;

import com.smartstudy.studyservice.dto.StudyStatsResponse;
import com.smartstudy.studyservice.model.StudySession;
import com.smartstudy.studyservice.repository.StudySessionRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StudyStatsServiceTest {

    @Test
    void getStatsShouldCalculateTotalSessionsAndAverage() {

        StudySessionRepository studySessionRepository =
                mock(StudySessionRepository.class);

        StudySession firstSession = new StudySession(
                1L,
                10L,
                Instant.parse("2026-09-25T10:00:00Z")
        );

        firstSession.setEndedAt(
                Instant.parse("2026-09-25T10:30:00Z")
        );
        firstSession.setDurationSeconds(1800L);

        StudySession secondSession = new StudySession(
                1L,
                20L,
                Instant.parse("2026-09-25T12:00:00Z")
        );

        secondSession.setEndedAt(
                Instant.parse("2026-09-25T13:00:00Z")
        );
        secondSession.setDurationSeconds(3600L);

        when(studySessionRepository
                .findByUserIdOrderByStartedAtDesc(1L))
                .thenReturn(List.of(firstSession, secondSession));

        StudyStatsService statsService =
                new StudyStatsService(studySessionRepository);

        StudyStatsResponse response =
                statsService.getStats(1L);

        assertEquals(5400L, response.getTotalStudySeconds());
        assertEquals(2L, response.getTotalSessions());
        assertEquals(2700.0, response.getAverageSessionSeconds());

        verify(studySessionRepository)
                .findByUserIdOrderByStartedAtDesc(1L);
    }

    @Test
    void getStatsShouldIgnoreActiveSessions() {

        StudySessionRepository studySessionRepository =
                mock(StudySessionRepository.class);

        StudySession completedSession = new StudySession(
                1L,
                10L,
                Instant.parse("2026-09-25T10:00:00Z")
        );

        completedSession.setEndedAt(
                Instant.parse("2026-09-25T10:30:00Z")
        );
        completedSession.setDurationSeconds(1800L);

        StudySession activeSession = new StudySession(
                1L,
                20L,
                Instant.parse("2026-09-25T12:00:00Z")
        );

        // No endedAt and no duration because this session is still running.

        when(studySessionRepository
                .findByUserIdOrderByStartedAtDesc(1L))
                .thenReturn(List.of(completedSession, activeSession));

        StudyStatsService statsService =
                new StudyStatsService(studySessionRepository);

        StudyStatsResponse response =
                statsService.getStats(1L);

        assertEquals(1800L, response.getTotalStudySeconds());
        assertEquals(1L, response.getTotalSessions());
        assertEquals(1800.0, response.getAverageSessionSeconds());
    }

    @Test
    void getStatsShouldReturnZerosWhenUserHasNoSessions() {

        StudySessionRepository studySessionRepository =
                mock(StudySessionRepository.class);

        when(studySessionRepository
                .findByUserIdOrderByStartedAtDesc(1L))
                .thenReturn(List.of());

        StudyStatsService statsService =
                new StudyStatsService(studySessionRepository);

        StudyStatsResponse response =
                statsService.getStats(1L);

        assertEquals(0L, response.getTotalStudySeconds());
        assertEquals(0L, response.getTotalSessions());
        assertEquals(0.0, response.getAverageSessionSeconds());
    }
}