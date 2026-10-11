package com.smartstudy.quizservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartstudy.quizservice.dto.QuestionRequest;
import com.smartstudy.quizservice.dto.QuestionResponse;
import com.smartstudy.quizservice.service.QuestionService;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class QuestionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private QuestionService questionService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);

        QuestionController controller =
                new QuestionController(questionService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();

        objectMapper = new ObjectMapper();
    }

    @Test
    void addQuestionShouldReturn201() throws Exception {

        QuestionRequest request = new QuestionRequest();
        request.setQuestionText("What does JVM stand for?");
        request.setOptionA("Java Virtual Machine");
        request.setOptionB("Java Version Manager");
        request.setOptionC("Java Visual Machine");
        request.setOptionD("Java Variable Method");
        request.setCorrectAnswer("A");

        QuestionResponse response =
                new QuestionResponse(
                        100L,
                        "What does JVM stand for?",
                        "Java Virtual Machine",
                        "Java Version Manager",
                        "Java Visual Machine",
                        "Java Variable Method"
                );

        when(questionService.addQuestion(
                eq(1L),
                eq(10L),
                any(QuestionRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/quizzes/10/questions")
                                .param("userId", "1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.questionText")
                        .value("What does JVM stand for?"))
                .andExpect(jsonPath("$.optionA")
                        .value("Java Virtual Machine"))
                .andExpect(jsonPath("$.optionB")
                        .value("Java Version Manager"))
                .andExpect(jsonPath("$.optionC")
                        .value("Java Visual Machine"))
                .andExpect(jsonPath("$.optionD")
                        .value("Java Variable Method"))
                .andExpect(jsonPath("$.correctAnswer")
                        .doesNotExist());

        verify(questionService)
                .addQuestion(
                        eq(1L),
                        eq(10L),
                        any(QuestionRequest.class)
                );
    }

    @Test
    void getQuestionsShouldReturnQuestions() throws Exception {

        List<QuestionResponse> questions = List.of(
                new QuestionResponse(
                        100L,
                        "What does JVM stand for?",
                        "Java Virtual Machine",
                        "Java Version Manager",
                        "Java Visual Machine",
                        "Java Variable Method"
                ),
                new QuestionResponse(
                        101L,
                        "Which keyword creates a class?",
                        "class",
                        "new",
                        "static",
                        "void"
                )
        );

        when(questionService.getQuestions(1L, 10L))
                .thenReturn(questions);

        mockMvc.perform(
                        get("/api/quizzes/10/questions")
                                .param("userId", "1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(100))
                .andExpect(jsonPath("$[0].questionText")
                        .value("What does JVM stand for?"))
                .andExpect(jsonPath("$[1].id").value(101))
                .andExpect(jsonPath("$[1].questionText")
                        .value("Which keyword creates a class?"))
                .andExpect(jsonPath("$[0].correctAnswer")
                        .doesNotExist())
                .andExpect(jsonPath("$[1].correctAnswer")
                        .doesNotExist());

        verify(questionService)
                .getQuestions(1L, 10L);
    }

    @Test
    void addQuestionShouldRejectMissingQuestionText() throws Exception {

        QuestionRequest request = new QuestionRequest();

        request.setQuestionText("");
        request.setOptionA("A");
        request.setOptionB("B");
        request.setOptionC("C");
        request.setOptionD("D");
        request.setCorrectAnswer("A");

        mockMvc.perform(
                        post("/api/quizzes/10/questions")
                                .param("userId", "1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        verify(questionService, never())
                .addQuestion(
                        eq(1L),
                        eq(10L),
                        any(QuestionRequest.class)
                );
    }

    @Test
    void addQuestionShouldRejectInvalidCorrectAnswer() throws Exception {

        QuestionRequest request = new QuestionRequest();

        request.setQuestionText("What does JVM stand for?");
        request.setOptionA("Java Virtual Machine");
        request.setOptionB("Java Version Manager");
        request.setOptionC("Java Visual Machine");
        request.setOptionD("Java Variable Method");
        request.setCorrectAnswer("X");

        mockMvc.perform(
                        post("/api/quizzes/10/questions")
                                .param("userId", "1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        verify(questionService, never())
                .addQuestion(
                        eq(1L),
                        eq(10L),
                        any(QuestionRequest.class)
                );
    }
}