package com.smartstudy.studyservice.dto;

public class StudyStatsResponse {

    private long totalStudySeconds;
    private long totalSessions;
    private double averageSessionSeconds;

    public StudyStatsResponse() {
    }

    public StudyStatsResponse(
            long totalStudySeconds,
            long totalSessions,
            double averageSessionSeconds) {

        this.totalStudySeconds = totalStudySeconds;
        this.totalSessions = totalSessions;
        this.averageSessionSeconds = averageSessionSeconds;
    }

    public long getTotalStudySeconds() {
        return totalStudySeconds;
    }

    public void setTotalStudySeconds(long totalStudySeconds) {
        this.totalStudySeconds = totalStudySeconds;
    }

    public long getTotalSessions() {
        return totalSessions;
    }

    public void setTotalSessions(long totalSessions) {
        this.totalSessions = totalSessions;
    }

    public double getAverageSessionSeconds() {
        return averageSessionSeconds;
    }

    public void setAverageSessionSeconds(double averageSessionSeconds) {
        this.averageSessionSeconds = averageSessionSeconds;
    }
}