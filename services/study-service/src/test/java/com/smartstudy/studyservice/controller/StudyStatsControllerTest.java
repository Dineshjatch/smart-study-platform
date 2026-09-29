package com.smartstudy.studyservice.controller;

import com.smartstudy.studyservice.dto.StudyStatsResponse;
import com.smartstudy.studyservice.service.StudyStatsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class StudyStatsControllerTest {

    private MockMvc mockMvc;

    @Mock
    private StudyStatsService studyStatsService;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);

        StudyStatsController controller =
                new StudyStatsController(studyStatsService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();
    }

    @Test
    void getStatsShouldReturnStatistics() throws Exception {

        StudyStatsResponse response =
                new StudyStatsResponse(
                        5400L,
                        2L,
                        2700.0
                );

        when(studyStatsService.getStats(1L))
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/study-stats")
                                .param("userId", "1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalStudySeconds").value(5400))
                .andExpect(jsonPath("$.totalSessions").value(2))
                .andExpect(jsonPath("$.averageSessionSeconds").value(2700.0));

        verify(studyStatsService)
                .getStats(1L);
    }

    @Test
    void getStatsShouldReturnZerosForNewUser() throws Exception {

        StudyStatsResponse response =
                new StudyStatsResponse(
                        0L,
                        0L,
                        0.0
                );

        when(studyStatsService.getStats(10L))
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/study-stats")
                                .param("userId", "10")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalStudySeconds").value(0))
                .andExpect(jsonPath("$.totalSessions").value(0))
                .andExpect(jsonPath("$.averageSessionSeconds").value(0.0));

        verify(studyStatsService)
                .getStats(10L);
    }
}