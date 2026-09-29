package com.smartstudy.studyservice.service;

import com.smartstudy.studyservice.dto.StudySessionRequest;
import com.smartstudy.studyservice.dto.StudySessionResponse;
import com.smartstudy.studyservice.model.StudySession;
import com.smartstudy.studyservice.model.Subject;
import com.smartstudy.studyservice.repository.StudySessionRepository;
import com.smartstudy.studyservice.repository.SubjectRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StudySessionServiceTest {

    @Test
    void startSessionShouldCreateNewSession() {

        StudySessionRepository studySessionRepository =
                mock(StudySessionRepository.class);

        SubjectRepository subjectRepository =
                mock(SubjectRepository.class);

        Subject subject = new Subject(
                1L,
                "Java",
                "Object-oriented programming"
        );

        when(studySessionRepository.findByUserIdAndEndedAtIsNull(1L))
                .thenReturn(Optional.empty());

        when(subjectRepository.findById(10L))
                .thenReturn(Optional.of(subject));

        when(studySessionRepository.save(any(StudySession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StudySessionService service =
                new StudySessionService(
                        studySessionRepository,
                        subjectRepository
                );

        StudySessionRequest request = new StudySessionRequest();
        request.setSubjectId(10L);

        StudySessionResponse response =
                service.startSession(1L, request);

        assertEquals(10L, response.getSubjectId());
        assertNotNull(response.getStartedAt());
        assertNull(response.getEndedAt());
        assertNull(response.getDurationSeconds());

        verify(studySessionRepository)
                .findByUserIdAndEndedAtIsNull(1L);

        verify(subjectRepository)
                .findById(10L);

        verify(studySessionRepository)
                .save(any(StudySession.class));
    }

    @Test
    void startSessionShouldRejectSecondActiveSession() {

        StudySessionRepository studySessionRepository =
                mock(StudySessionRepository.class);

        SubjectRepository subjectRepository =
                mock(SubjectRepository.class);

        StudySession activeSession = new StudySession(
                1L,
                10L,
                Instant.now()
        );

        when(studySessionRepository.findByUserIdAndEndedAtIsNull(1L))
                .thenReturn(Optional.of(activeSession));

        StudySessionService service =
                new StudySessionService(
                        studySessionRepository,
                        subjectRepository
                );

        StudySessionRequest request = new StudySessionRequest();
        request.setSubjectId(20L);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.startSession(1L, request)
        );

        assertEquals(
                "A study session is already running",
                exception.getMessage()
        );

        verify(subjectRepository, never()).findById(anyLong());
        verify(studySessionRepository, never()).save(any(StudySession.class));
    }

    @Test
    void startSessionShouldRejectSubjectOwnedByAnotherUser() {

        StudySessionRepository studySessionRepository =
                mock(StudySessionRepository.class);

        SubjectRepository subjectRepository =
                mock(SubjectRepository.class);

        Subject subject = new Subject(
                2L,
                "Private Subject",
                "Owned by another user"
        );

        when(studySessionRepository.findByUserIdAndEndedAtIsNull(1L))
                .thenReturn(Optional.empty());

        when(subjectRepository.findById(10L))
                .thenReturn(Optional.of(subject));

        StudySessionService service =
                new StudySessionService(
                        studySessionRepository,
                        subjectRepository
                );

        StudySessionRequest request = new StudySessionRequest();
        request.setSubjectId(10L);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.startSession(1L, request)
        );

        assertEquals(
                "Subject does not belong to this user",
                exception.getMessage()
        );

        verify(studySessionRepository, never())
                .save(any(StudySession.class));
    }

    @Test
    void stopSessionShouldCalculateDuration() {

        StudySessionRepository studySessionRepository =
                mock(StudySessionRepository.class);

        SubjectRepository subjectRepository =
                mock(SubjectRepository.class);

        Instant startedAt = Instant.now().minusSeconds(3600);

        StudySession session = new StudySession(
                1L,
                10L,
                startedAt
        );

        when(studySessionRepository.findByUserIdAndEndedAtIsNull(1L))
                .thenReturn(Optional.of(session));

        when(studySessionRepository.save(session))
                .thenReturn(session);

        StudySessionService service =
                new StudySessionService(
                        studySessionRepository,
                        subjectRepository
                );

        StudySessionResponse response =
                service.stopSession(1L);

        assertNotNull(response.getEndedAt());
        assertNotNull(response.getDurationSeconds());

        // The test started the session one hour ago, so
        // the duration should be close to 3600 seconds.
        assertTrue(response.getDurationSeconds() >= 3599);
        assertTrue(response.getDurationSeconds() <= 3601);

        verify(studySessionRepository)
                .save(session);
    }

    @Test
    void stopSessionShouldFailWhenNoActiveSessionExists() {

        StudySessionRepository studySessionRepository =
                mock(StudySessionRepository.class);

        SubjectRepository subjectRepository =
                mock(SubjectRepository.class);

        when(studySessionRepository.findByUserIdAndEndedAtIsNull(1L))
                .thenReturn(Optional.empty());

        StudySessionService service =
                new StudySessionService(
                        studySessionRepository,
                        subjectRepository
                );

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.stopSession(1L)
        );

        assertEquals(
                "No active study session found",
                exception.getMessage()
        );

        verify(studySessionRepository, never())
                .save(any(StudySession.class));
    }

    @Test
    void getHistoryShouldReturnUsersSessions() {

        StudySessionRepository studySessionRepository =
                mock(StudySessionRepository.class);

        SubjectRepository subjectRepository =
                mock(SubjectRepository.class);

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
                Instant.parse("2026-09-24T15:00:00Z")
        );

        secondSession.setEndedAt(
                Instant.parse("2026-09-24T16:00:00Z")
        );
        secondSession.setDurationSeconds(3600L);

        when(studySessionRepository
                .findByUserIdOrderByStartedAtDesc(1L))
                .thenReturn(List.of(firstSession, secondSession));

        StudySessionService service =
                new StudySessionService(
                        studySessionRepository,
                        subjectRepository
                );

        List<StudySessionResponse> history =
                service.getHistory(1L);

        assertEquals(2, history.size());

        assertEquals(
                10L,
                history.get(0).getSubjectId()
        );

        assertEquals(
                1800L,
                history.get(0).getDurationSeconds()
        );

        assertEquals(
                20L,
                history.get(1).getSubjectId()
        );

        assertEquals(
                3600L,
                history.get(1).getDurationSeconds()
        );

        verify(studySessionRepository)
                .findByUserIdOrderByStartedAtDesc(1L);
    }
}