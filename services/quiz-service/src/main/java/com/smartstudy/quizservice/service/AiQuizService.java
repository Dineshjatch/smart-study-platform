package com.smartstudy.quizservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
//import tools.jackson.databind.ObjectMapper;
import com.openai.client.OpenAIClient;
import com.openai.models.ChatModel;
import com.openai.models.responses.ResponseCreateParams;
import com.smartstudy.quizservice.dto.AiQuizGenerateRequest;
import com.smartstudy.quizservice.dto.AiQuizGenerateResponse;
import com.smartstudy.quizservice.dto.QuestionResponse;
import com.smartstudy.quizservice.model.Question;
import com.smartstudy.quizservice.model.Quiz;
import com.smartstudy.quizservice.repository.QuestionRepository;
import com.smartstudy.quizservice.repository.QuizRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class AiQuizService {

    private final OpenAIClient openAIClient;
    private final ObjectMapper objectMapper;
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;

    public AiQuizService(
            OpenAIClient openAIClient,
            ObjectMapper objectMapper,
            QuizRepository quizRepository,
            QuestionRepository questionRepository
    ) {
        this.openAIClient = openAIClient;
        this.objectMapper = objectMapper;
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
    }

    @Transactional
    public AiQuizGenerateResponse generateQuiz(
            Long userId,
            AiQuizGenerateRequest request
    ) {
        String prompt = buildPrompt(request);

        ResponseCreateParams params = ResponseCreateParams.builder()
                .input(prompt)
                .model("gpt-4.1-mini")
                .build();

        var response = openAIClient.responses().create(params);

        String aiText = extractText(response);

        AiQuizData quizData = parseResponse(aiText);

        if (quizData.title == null || quizData.title.isBlank()) {
            throw new IllegalStateException("AI returned an invalid quiz title");
        }

        if (quizData.questions == null || quizData.questions.isEmpty()) {
            throw new IllegalStateException("AI did not generate any questions");
        }

        Quiz quiz = new Quiz(
                userId,
                quizData.title,
                quizData.description,
                true,
                Instant.now()
        );

        Quiz savedQuiz = quizRepository.save(quiz);

        for (AiQuestionData questionData : quizData.questions) {

            String correctAnswer =
                    normalizeCorrectAnswer(questionData.correctAnswer);

            Question question = new Question(
                    savedQuiz.getId(),
                    questionData.questionText,
                    questionData.optionA,
                    questionData.optionB,
                    questionData.optionC,
                    questionData.optionD,
                    correctAnswer
            );

            questionRepository.save(question);
        }

        List<QuestionResponse> questionResponses =
                questionRepository.findByQuizId(savedQuiz.getId())
                        .stream()
                        .map(this::toQuestionResponse)
                        .toList();

        return new AiQuizGenerateResponse(
                savedQuiz.getId(),
                savedQuiz.getTitle(),
                savedQuiz.getDescription(),
                true,
                questionResponses
        );
    }

    private String buildPrompt(AiQuizGenerateRequest request) {

        return """
                Generate a multiple-choice quiz.

                Topic: %s
                Number of questions: %d
                Difficulty: %s

                Return ONLY valid JSON.
                Do not use markdown.
                Do not wrap the JSON in ```json or ```.

                Use exactly this structure:

                {
                  "title": "Quiz title",
                  "description": "Short quiz description",
                  "questions": [
                    {
                      "questionText": "Question text",
                      "optionA": "Option A",
                      "optionB": "Option B",
                      "optionC": "Option C",
                      "optionD": "Option D",
                      "correctAnswer": "A"
                    }
                  ]
                }

                Rules:
                - Generate exactly %d questions.
                - Every question must have four options.
                - correctAnswer must be only A, B, C, or D.
                - Questions must be relevant to the requested topic.
                - Avoid duplicate questions.
                """.formatted(
                request.getTopic(),
                request.getNumberOfQuestions(),
                request.getDifficulty(),
                request.getNumberOfQuestions()
        );
    }

    private String extractText(
            com.openai.models.responses.Response response
    ) {
        StringBuilder text = new StringBuilder();

        response.output()
                .stream()
                .flatMap(item -> item.message().stream())
                .flatMap(message -> message.content().stream())
                .flatMap(content -> content.outputText().stream())
                .forEach(outputText -> text.append(outputText.text()));

        if (text.isEmpty()) {
            throw new IllegalStateException("AI returned an empty response");
        }

        return cleanJson(text.toString());
    }

    private String cleanJson(String text) {

        String cleaned = text.trim();

        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }

        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }

        return cleaned.trim();
    }

    private AiQuizData parseResponse(String aiText) {

        try {
            return objectMapper.readValue(aiText, AiQuizData.class);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "AI returned an invalid quiz format",
                    e
            );
        }
    }

    private String normalizeCorrectAnswer(String correctAnswer) {

        if (correctAnswer == null) {
            throw new IllegalStateException(
                    "AI returned a question without a correct answer"
            );
        }

        String answer = correctAnswer.trim().toUpperCase();

        if (!answer.matches("[ABCD]")) {
            throw new IllegalStateException(
                    "AI returned an invalid correct answer"
            );
        }

        return answer;
    }

    private QuestionResponse toQuestionResponse(Question question) {

        return new QuestionResponse(
                question.getId(),
                question.getQuestionText(),
                question.getOptionA(),
                question.getOptionB(),
                question.getOptionC(),
                question.getOptionD()
        );
    }

    /*
     * These classes represent the JSON structure expected from the AI.
     * They are private because they are only used internally while
     * converting the AI response into database entities.
     */
    private static class AiQuizData {

        public String title;
        public String description;
        public List<AiQuestionData> questions;
    }

    private static class AiQuestionData {

        public String questionText;
        public String optionA;
        public String optionB;
        public String optionC;
        public String optionD;
        public String correctAnswer;
    }
}