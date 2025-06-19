package com.munetmo.lingetic.LanguageTestService.UseCases;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptRequests.FillInTheBlanksAttemptRequest;
import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptRequests.TranslationAttemptRequest;
import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptResponses.AttemptResponse;
import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptResponses.TranslationAttemptResponse;
import com.munetmo.lingetic.LanguageTestService.Entities.AttemptStatus;
import com.munetmo.lingetic.LanguageService.Entities.Language;
import com.munetmo.lingetic.LanguageTestService.Exceptions.QuestionNotFoundException;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.FillInTheBlanksQuestion;
import com.munetmo.lingetic.LanguageTestService.infra.Repositories.Postgres.QuestionPostgresRepository;
import com.munetmo.lingetic.LanguageTestService.Entities.Sentence;
import com.munetmo.lingetic.LanguageTestService.infra.Repositories.Postgres.SentencePostgresRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
class AttemptQuestionUseCaseTest {
    @Container
    @ServiceConnection
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Container
    @ServiceConnection
    private static final RabbitMQContainer rabbitmq = new RabbitMQContainer("rabbitmq:4-management");

    @DynamicPropertySource
    static void registerPgAndRabbitMQProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.rabbitmq.uri", rabbitmq::getAmqpUrl);
    }

    @Autowired
    private AttemptQuestionUseCase attemptQuestionUseCase;

    @Autowired
    private QuestionPostgresRepository questionRepository;

    @Autowired
    private SentencePostgresRepository sentenceRepository;

    private static final String TEST_USER_ID = UUID.randomUUID().toString();
    private static final String TEST_SENTENCE_ID = UUID.randomUUID().toString();

    @BeforeEach
    void setUp() {
        questionRepository.deleteAllQuestions();
        sentenceRepository.deleteAllSentences();

        // Create a test sentence
        sentenceRepository.addSentence(new Sentence(
                UUID.fromString(TEST_SENTENCE_ID),
                Language.English,
                "The cat stretched lazily on the windowsill.",
                Language.Turkish,
                "Kedi pencere eşiğinde tembelce gerildi.",
                10,
                List.of()
        ));
    }

    @Test
    void shouldReturnCorrectResponseForValidAttempt() throws QuestionNotFoundException, JsonProcessingException {
        var question = new FillInTheBlanksQuestion(
                UUID.randomUUID().toString(),
                Language.English,
                "The cat ____ lazily on the windowsill.",
                "straighten or extend one's body",
                "stretched",
                TEST_SENTENCE_ID,
                List.of()
        );
        questionRepository.addQuestion(question);
        var request = new FillInTheBlanksAttemptRequest(TEST_SENTENCE_ID, "stretched");

        AttemptResponse response = attemptQuestionUseCase.execute(TEST_USER_ID, request);

        assertNotNull(response);
        assertSame(AttemptStatus.Success, response.getAttemptStatus());
    }

    @Test
    void shouldReturnIncorrectResponseForInvalidAttempt() throws QuestionNotFoundException, JsonProcessingException {
        var question = new FillInTheBlanksQuestion(
                UUID.randomUUID().toString(),
                Language.English,
                "The cat ____ lazily on the windowsill.",
                "straighten or extend one's body",
                "stretched",
                TEST_SENTENCE_ID,
                List.of()
        );
        questionRepository.addQuestion(question);
        var request = new FillInTheBlanksAttemptRequest(TEST_SENTENCE_ID, "wrong answer");

        AttemptResponse response = attemptQuestionUseCase.execute(TEST_USER_ID, request);

        assertNotNull(response);
        assertSame(AttemptStatus.Failure, response.getAttemptStatus());
    }

    @Test
    void shouldThrowQuestionNotFoundExceptionForNonexistentQuestion() {
        var request = new FillInTheBlanksAttemptRequest(UUID.randomUUID().toString(), "test answer");

        assertThrows(QuestionNotFoundException.class, () -> attemptQuestionUseCase.execute(TEST_USER_ID, request));
    }

    @Test
    void shouldHandleTranslationSpecially() throws QuestionNotFoundException, JsonProcessingException {
        // For Translation questions, the use case should create a question dynamically from sentence data instead of
        // querying the question repository
        var request = new TranslationAttemptRequest(TEST_SENTENCE_ID, "The cat stretched lazily on the windowsill.");

        AttemptResponse response = attemptQuestionUseCase.execute(TEST_USER_ID, request);

        assertNotNull(response);
        assertTrue(response instanceof TranslationAttemptResponse);
        var typedResponse = (TranslationAttemptResponse) response;
        assertSame(AttemptStatus.Success, response.getAttemptStatus());
        assertEquals("The cat stretched lazily on the windowsill.", typedResponse.getCorrectAnswer());
    }

    @Test
    void shouldReturnFailureForIncorrectTranslation() throws QuestionNotFoundException, JsonProcessingException {
        var request = new TranslationAttemptRequest(TEST_SENTENCE_ID, "Wrong translation");

        AttemptResponse response = attemptQuestionUseCase.execute(TEST_USER_ID, request);

        assertNotNull(response);
        assertTrue(response instanceof TranslationAttemptResponse);
        var typedResponse = (TranslationAttemptResponse) response;
        assertSame(AttemptStatus.Failure, response.getAttemptStatus());
        assertEquals("The cat stretched lazily on the windowsill.", typedResponse.getCorrectAnswer());
    }

    @Test
    void shouldNotQueryQuestionRepositoryForTranslation() throws QuestionNotFoundException, JsonProcessingException {
        // Verify that no question exists in the repository for this sentence
        assertThrows(QuestionNotFoundException.class, () ->
            questionRepository.getQuestionBySentenceID(TEST_SENTENCE_ID));

        // But codeTranslation should still work because it creates the question dynamically
        var request = new TranslationAttemptRequest(TEST_SENTENCE_ID, "The cat stretched lazily on the windowsill.");

        AttemptResponse response = attemptQuestionUseCase.execute(TEST_USER_ID, request);

        assertNotNull(response);
        assertSame(AttemptStatus.Success, response.getAttemptStatus());
    }

    @Test
    void shouldThrowQuestionNotFoundExceptionForTranslationWithNonexistentSentence() {
        var nonexistentSentenceId = UUID.randomUUID().toString();
        var request = new TranslationAttemptRequest(nonexistentSentenceId, "Any translation");

        // Should throw exception because the sentence doesn't exist
        assertThrows(Exception.class, () -> attemptQuestionUseCase.execute(TEST_USER_ID, request));
    }
}
