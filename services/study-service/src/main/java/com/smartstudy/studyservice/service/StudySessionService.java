package com.smartstudy.studyservice.service;

import com.smartstudy.studyservice.dto.StudySessionRequest;
import com.smartstudy.studyservice.dto.StudySessionResponse;
import com.smartstudy.studyservice.model.StudySession;
import com.smartstudy.studyservice.model.Subject;
import com.smartstudy.studyservice.repository.StudySessionRepository;
import com.smartstudy.studyservice.repository.SubjectRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class StudySessionService {

    private final StudySessionRepository studySessionRepository;
    private final SubjectRepository subjectRepository;

    public StudySessionService(
            StudySessionRepository studySessionRepository,
            SubjectRepository subjectRepository) {

        this.studySessionRepository = studySessionRepository;
        this.subjectRepository = subjectRepository;
    }

    public StudySessionResponse startSession(
            Long userId,
            StudySessionRequest request) {

        // A student can have only one running timer at a time.
        if (studySessionRepository.findByUserIdAndEndedAtIsNull(userId).isPresent()) {
            throw new IllegalStateException(
                    "A study session is already running"
            );
        }

        // The subject must belong to the same user who is starting the timer.
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Subject not found"));

        if (!subject.getUserId().equals(userId)) {
            throw new IllegalArgumentException(
                    "Subject does not belong to this user"
            );
        }

        StudySession session = new StudySession(
                userId,
                request.getSubjectId(),
                Instant.now()
        );

        StudySession savedSession =
                studySessionRepository.save(session);

        return toResponse(savedSession);
    }

    public StudySessionResponse stopSession(Long userId) {

        StudySession session =
                studySessionRepository.findByUserIdAndEndedAtIsNull(userId)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "No active study session found"
                                ));

        Instant endedAt = Instant.now();

        session.setEndedAt(endedAt);

        long durationSeconds = Duration.between(
                session.getStartedAt(),
                endedAt
        ).getSeconds();

        session.setDurationSeconds(durationSeconds);

        StudySession savedSession =
                studySessionRepository.save(session);

        return toResponse(savedSession);
    }

    public List<StudySessionResponse> getHistory(Long userId) {

        return studySessionRepository
                .findByUserIdOrderByStartedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private StudySessionResponse toResponse(StudySession session) {

        return new StudySessionResponse(
                session.getId(),
                session.getSubjectId(),
                session.getStartedAt(),
                session.getEndedAt(),
                session.getDurationSeconds()
        );
    }
}