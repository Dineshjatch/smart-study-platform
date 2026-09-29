package com.smartstudy.studyservice.repository;

import com.smartstudy.studyservice.model.StudySession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudySessionRepository
        extends JpaRepository<StudySession, Long> {

    List<StudySession> findByUserIdOrderByStartedAtDesc(Long userId);

    Optional<StudySession> findByIdAndUserId(Long id, Long userId);

    Optional<StudySession> findByUserIdAndEndedAtIsNull(Long userId);
}