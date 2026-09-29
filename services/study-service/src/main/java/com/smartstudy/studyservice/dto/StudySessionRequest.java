package com.smartstudy.studyservice.dto;

import jakarta.validation.constraints.NotNull;

public class StudySessionRequest {

    @NotNull(message = "Subject ID is required")
    private Long subjectId;

    public StudySessionRequest() {
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }
}