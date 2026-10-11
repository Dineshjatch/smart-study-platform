package com.smartstudy.quizservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.client.OpenAIClient;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseOutputItem;
import com.openai.models.responses.ResponseOutputMessage;
import com.openai.models.responses.ResponseOutputText;
import com.openai.services.blocking.ResponseService;
import com.smartstudy.quizservice.dto.AiQuizGenerateRequest;
import com.smartstudy.quizservice.dto.AiQuizGenerateResponse;
import com.smartstudy.quizservice.model.Question;
import com.smartstudy.quizservice.model.Quiz;
import com.smartstudy.quizservice.repository.QuestionRepository;
import com.smartstudy.quizservice.repository.QuizRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiQuizServiceTest {

    @Mock
    private OpenAIClient openAIClient;

    @Mock
    private ResponseService responseService;

    @Mock
    private Response response;

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private QuestionRepository questionRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void generateQuizShouldCreateQuizAndQuestions() {

        String aiJson = """
                {
                  "title": "Java Collections Quiz",
                  "description": "A quiz about Java Collections",
                  "questions": [
                    {
                      "questionText": "Which interface stores unique elements?",
                      "optionA": "List",
                      "optionB": "Set",
                      "optionC": "Queue",
                      "optionD": "Array",
                      "correctAnswer": "B"
                    }
                  ]
                }
                """;

        ResponseOutputText outputText = ResponseOutputText.builder()
                .text(aiJson)
                .annotations(List.of())
                .build();

        ResponseOutputMessage message = ResponseOutputMessage.builder()
                .id("msg_test_1")
                .status(ResponseOutputMessage.Status.COMPLETED)
                .addContent(outputText)
                .build();

        when(openAIClient.responses()).thenReturn(responseService);

        when(responseService.create(any(ResponseCreateParams.class)))
                .thenReturn(response);

        when(response.output())
                .thenReturn(List.of(ResponseOutputItem.ofMessage(message)));

        Quiz savedQuiz = new Quiz(
                1L,
                "Java Collections Quiz",
                "A quiz about Java Collections",
                true,
                Instant.now()
        );

        setQuizId(savedQuiz, 100L);

        when(quizRepository.save(any(Quiz.class)))
                .thenReturn(savedQuiz);

        Question savedQuestion = new Question(
                100L,
                "Which interface stores unique elements?",
                "List",
                "Set",
                "Queue",
                "Array",
                "B"
        );

        setQuestionId(savedQuestion, 500L);

        when(questionRepository.save(any(Question.class)))
                .thenReturn(savedQuestion);

        when(questionRepository.findByQuizId(100L))
                .thenReturn(List.of(savedQuestion));

        AiQuizGenerateRequest request = new AiQuizGenerateRequest();
        request.setTopic("Java Collections");
        request.setNumberOfQuestions(1);
        request.setDifficulty("medium");

        AiQuizService aiQuizService = new AiQuizService(
                openAIClient,
                objectMapper,
                quizRepository,
                questionRepository
        );

        AiQuizGenerateResponse result =
                aiQuizService.generateQuiz(1L, request);

        assertEquals(100L, result.getQuizId());
        assertEquals("Java Collections Quiz", result.getTitle());
        assertEquals("A quiz about Java Collections", result.getDescription());
        assertTrue(result.isAiGenerated());

        assertEquals(1, result.getQuestions().size());
        assertEquals(
                "Which interface stores unique elements?",
                result.getQuestions().get(0).getQuestionText()
        );

        verify(openAIClient).responses();
        verify(responseService).create(any(ResponseCreateParams.class));
        verify(quizRepository).save(any(Quiz.class));
        verify(questionRepository).save(any(Question.class));
        verify(questionRepository).findByQuizId(100L);
    }

    @Test
    void generateQuizShouldRejectInvalidCorrectAnswer() {

        String aiJson = """
                {
                  "title": "Java Quiz",
                  "description": "Basic Java quiz",
                  "questions": [
                    {
                      "questionText": "What is Java?",
                      "optionA": "Language",
                      "optionB": "Database",
                      "optionC": "OS",
                      "optionD": "Browser",
                      "correctAnswer": "X"
                    }
                  ]
                }
                """;

        ResponseOutputText outputText = ResponseOutputText.builder()
                .text(aiJson)
                .annotations(List.of())
                .build();

        ResponseOutputMessage message = ResponseOutputMessage.builder()
                .id("msg_test_2")
                .status(ResponseOutputMessage.Status.COMPLETED)
                .addContent(outputText)
                .build();

        when(openAIClient.responses()).thenReturn(responseService);

        when(responseService.create(any(ResponseCreateParams.class)))
                .thenReturn(response);

        when(response.output())
                .thenReturn(List.of(ResponseOutputItem.ofMessage(message)));

        AiQuizGenerateRequest request = new AiQuizGenerateRequest();
        request.setTopic("Java");
        request.setNumberOfQuestions(1);
        request.setDifficulty("easy");

        AiQuizService aiQuizService = new AiQuizService(
                openAIClient,
                objectMapper,
                quizRepository,
                questionRepository
        );

        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalStateException.class,
                () -> aiQuizService.generateQuiz(1L, request)
        );
    }

    @Test
    void generateQuizShouldRejectInvalidJson() {

        String invalidJson = """
                This is not valid JSON
                """;

        ResponseOutputText outputText = ResponseOutputText.builder()
                .text(invalidJson)
                .annotations(List.of())
                .build();

        ResponseOutputMessage message = ResponseOutputMessage.builder()
                .id("msg_test_3")
                .status(ResponseOutputMessage.Status.COMPLETED)
                .addContent(outputText)
                .build();

        when(openAIClient.responses()).thenReturn(responseService);

        when(responseService.create(any(ResponseCreateParams.class)))
                .thenReturn(response);

        when(response.output())
                .thenReturn(List.of(ResponseOutputItem.ofMessage(message)));

        AiQuizGenerateRequest request = new AiQuizGenerateRequest();
        request.setTopic("Java");
        request.setNumberOfQuestions(1);
        request.setDifficulty("easy");

        AiQuizService aiQuizService = new AiQuizService(
                openAIClient,
                objectMapper,
                quizRepository,
                questionRepository
        );

        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalStateException.class,
                () -> aiQuizService.generateQuiz(1L, request)
        );
    }

    @Test
    void generateQuizShouldRejectEmptyAiResponse() {

        when(openAIClient.responses()).thenReturn(responseService);

        when(responseService.create(any(ResponseCreateParams.class)))
                .thenReturn(response);

        when(response.output())
                .thenReturn(List.of());

        AiQuizGenerateRequest request = new AiQuizGenerateRequest();
        request.setTopic("Java");
        request.setNumberOfQuestions(1);
        request.setDifficulty("easy");

        AiQuizService aiQuizService = new AiQuizService(
                openAIClient,
                objectMapper,
                quizRepository,
                questionRepository
        );

        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalStateException.class,
                () -> aiQuizService.generateQuiz(1L, request)
        );
    }

    private void setQuizId(Quiz quiz, Long id) {
        try {
            var field = Quiz.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(quiz, id);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Could not set test quiz ID", e);
        }
    }

    private void setQuestionId(Question question, Long id) {
        try {
            var field = Question.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(question, id);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Could not set test question ID", e);
        }
    }
}