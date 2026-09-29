package com.smartstudy.studyservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartstudy.studyservice.dto.SubjectRequest;
import com.smartstudy.studyservice.dto.SubjectResponse;
import com.smartstudy.studyservice.service.SubjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class SubjectControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SubjectService subjectService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);

        SubjectController controller =
                new SubjectController(subjectService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();

        objectMapper = new ObjectMapper();
    }

    @Test
    void createSubjectShouldReturn201() throws Exception {

        SubjectRequest request = new SubjectRequest();
        request.setName("Java");
        request.setDescription("Object-oriented programming");

        SubjectResponse response = new SubjectResponse(
                10L,
                "Java",
                "Object-oriented programming"
        );

        when(subjectService.createSubject(
                eq(1L),
                any(SubjectRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/subjects")
                                .param("userId", "1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("Java"))
                .andExpect(jsonPath("$.description")
                        .value("Object-oriented programming"));

        verify(subjectService)
                .createSubject(eq(1L), any(SubjectRequest.class));
    }

    @Test
    void createSubjectShouldRejectBlankName() throws Exception {

        SubjectRequest request = new SubjectRequest();
        request.setName("");
        request.setDescription("Invalid subject");

        mockMvc.perform(
                        post("/api/subjects")
                                .param("userId", "1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        verify(subjectService, org.mockito.Mockito.never())
                .createSubject(
                        any(Long.class),
                        any(SubjectRequest.class)
                );
    }

    @Test
    void getSubjectsShouldReturnUsersSubjects() throws Exception {

        List<SubjectResponse> subjects = List.of(
                new SubjectResponse(
                        1L,
                        "Java",
                        "Programming"
                ),
                new SubjectResponse(
                        2L,
                        "DBMS",
                        "Databases"
                )
        );

        when(subjectService.getSubjectsByUser(1L))
                .thenReturn(subjects);

        mockMvc.perform(
                        get("/api/subjects")
                                .param("userId", "1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Java"))
                .andExpect(jsonPath("$[1].name").value("DBMS"));

        verify(subjectService)
                .getSubjectsByUser(1L);
    }

    @Test
    void getSubjectShouldReturnSubject() throws Exception {

        SubjectResponse response = new SubjectResponse(
                10L,
                "Operating Systems",
                "Processes and memory"
        );

        when(subjectService.getSubject(1L, 10L))
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/subjects/10")
                                .param("userId", "1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name")
                        .value("Operating Systems"));

        verify(subjectService)
                .getSubject(1L, 10L);
    }

    @Test
    void updateSubjectShouldReturnUpdatedSubject() throws Exception {

        SubjectRequest request = new SubjectRequest();
        request.setName("Advanced Java");
        request.setDescription("Advanced concepts");

        SubjectResponse response = new SubjectResponse(
                10L,
                "Advanced Java",
                "Advanced concepts"
        );

        when(subjectService.updateSubject(
                eq(1L),
                eq(10L),
                any(SubjectRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                        put("/api/subjects/10")
                                .param("userId", "1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name")
                        .value("Advanced Java"))
                .andExpect(jsonPath("$.description")
                        .value("Advanced concepts"));

        verify(subjectService)
                .updateSubject(
                        eq(1L),
                        eq(10L),
                        any(SubjectRequest.class)
                );
    }

    @Test
    void deleteSubjectShouldReturn204() throws Exception {

        mockMvc.perform(
                        delete("/api/subjects/10")
                                .param("userId", "1")
                )
                .andExpect(status().isNoContent());

        verify(subjectService)
                .deleteSubject(1L, 10L);
    }
}