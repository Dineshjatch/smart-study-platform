package com.smartstudy.studyservice.dto;

import java.time.Instant;

public class StudySessionResponse {

    private Long id;
    private Long subjectId;
    private Instant startedAt;
    private Instant endedAt;
    private Long durationSeconds;

    public StudySessionResponse() {
    }

    public StudySessionResponse(
            Long id,
            Long subjectId,
            Instant startedAt,
            Instant endedAt,
            Long durationSeconds) {

        this.id = id;
        this.subjectId = subjectId;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.durationSeconds = durationSeconds;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(Instant endedAt) {
        this.endedAt = endedAt;
    }

    public Long getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Long durationSeconds) {
        this.durationSeconds = durationSeconds;
    }
}