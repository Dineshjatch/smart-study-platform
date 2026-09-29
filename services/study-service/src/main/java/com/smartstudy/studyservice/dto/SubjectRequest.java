package com.smartstudy.studyservice.dto;

import jakarta.validation.constraints.NotBlank;

public class SubjectRequest {

    @NotBlank(message = "Subject name is required")
    private String name;

    private String description;

    public SubjectRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}