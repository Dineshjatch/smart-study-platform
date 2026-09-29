package com.smartstudy.studyservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartstudy.studyservice.dto.StudySessionRequest;
import com.smartstudy.studyservice.dto.StudySessionResponse;
import com.smartstudy.studyservice.service.StudySessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class StudySessionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private StudySessionService studySessionService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);

        StudySessionController controller =
                new StudySessionController(studySessionService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();

        objectMapper = new ObjectMapper();
    }

    @Test
    void startSessionShouldReturn201() throws Exception {

        StudySessionRequest request = new StudySessionRequest();
        request.setSubjectId(10L);

        StudySessionResponse response =
                new StudySessionResponse(
                        100L,
                        10L,
                        Instant.parse("2026-09-25T10:00:00Z"),
                        null,
                        null
                );



        when(studySessionService.startSession(
                eq(1L),
                any(StudySessionRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/study-sessions/start")
                                .param("userId", "1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.subjectId").value(10))
                .andExpect(jsonPath("$.endedAt").doesNotExist())
                .andExpect(jsonPath("$.durationSeconds").doesNotExist());


        verify(studySessionService)
                .startSession(
                        eq(1L),
                        any(StudySessionRequest.class)
                );
    }

    @Test
    void stopSessionShouldReturn200() throws Exception {

        StudySessionResponse response =
                new StudySessionResponse(
                        100L,
                        10L,
                        Instant.parse("2026-09-25T10:00:00Z"),
                        Instant.parse("2026-09-25T10:45:00Z"),
                        2700L
                );

        when(studySessionService.stopSession(1L))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/study-sessions/stop")
                                .param("userId", "1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.subjectId").value(10))
                .andExpect(jsonPath("$.durationSeconds").value(2700));

        verify(studySessionService)
                .stopSession(1L);
    }

    @Test
    void getHistoryShouldReturnSessions() throws Exception {

        List<StudySessionResponse> history = List.of(
                new StudySessionResponse(
                        100L,
                        10L,
                        Instant.parse("2026-09-25T10:00:00Z"),
                        Instant.parse("2026-09-25T10:45:00Z"),
                        2700L
                ),
                new StudySessionResponse(
                        101L,
                        20L,
                        Instant.parse("2026-09-24T15:00:00Z"),
                        Instant.parse("2026-09-24T15:30:00Z"),
                        1800L
                )
        );

        when(studySessionService.getHistory(1L))
                .thenReturn(history);

        mockMvc.perform(
                        get("/api/study-sessions/history")
                                .param("userId", "1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].subjectId").value(10))
                .andExpect(jsonPath("$[0].durationSeconds").value(2700))
                .andExpect(jsonPath("$[1].subjectId").value(20))
                .andExpect(jsonPath("$[1].durationSeconds").value(1800));

        verify(studySessionService)
                .getHistory(1L);
    }
}