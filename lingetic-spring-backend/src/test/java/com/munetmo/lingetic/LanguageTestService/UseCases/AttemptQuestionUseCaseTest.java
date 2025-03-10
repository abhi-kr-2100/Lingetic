package com.munetmo.lingetic.LanguageTestService.UseCases;

import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptRequests.FillInTheBlanksAttemptRequest;
import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptResponses.AttemptResponse;
import com.munetmo.lingetic.LanguageTestService.Entities.AttemptStatus;
import com.munetmo.lingetic.LanguageTestService.Entities.Language;
import com.munetmo.lingetic.LanguageTestService.Exceptions.QuestionNotFoundException;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.FillInTheBlanksQuestion;
import com.munetmo.lingetic.LanguageTestService.infra.Repositories.Postgres.QuestionPostgresRepository;
import com.munetmo.lingetic.LanguageTestService.infra.Repositories.Postgres.QuestionReviewPostgresRepository;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AttemptQuestionUseCaseTest {
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");
    private AttemptQuestionUseCase attemptQuestionUseCase;
    private static QuestionPostgresRepository questionRepository;
    private static QuestionReviewPostgresRepository questionReviewRepository;
    private static final String TEST_USER_ID = UUID.randomUUID().toString();
    private static final String TEST_QUESTION_LIST_ID = UUID.randomUUID().toString();

    @BeforeAll
    static void beforeAll() {
        postgres.start();

        var dataSource = DataSourceBuilder.create()
                .url(postgres.getJdbcUrl())
                .username(postgres.getUsername())
                .password(postgres.getPassword())
                .build();
        var jdbcTemplate = new JdbcTemplate(dataSource);

        questionRepository = new QuestionPostgresRepository(jdbcTemplate);
        questionReviewRepository = new QuestionReviewPostgresRepository(jdbcTemplate);

        var flyway = Flyway.configure()
                .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                .load();
        flyway.migrate();
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
    }

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> false);
    }

    @BeforeEach
    void setUp() {
        questionReviewRepository.deleteAllReviews();
        questionRepository.deleteAllQuestions();
        attemptQuestionUseCase = new AttemptQuestionUseCase(questionRepository, questionReviewRepository);

        questionRepository.addQuestion(new FillInTheBlanksQuestion(
            UUID.randomUUID().toString(),
            Language.English,
            "The cat ____ lazily on the windowsill.",
            "straighten or extend one's body",
            "stretched",
            0,
            TEST_QUESTION_LIST_ID
        ));
    }

    @Test
    void shouldReturnCorrectResponseForValidAttempt() throws QuestionNotFoundException {
        var question = new FillInTheBlanksQuestion(
            UUID.randomUUID().toString(),
            Language.English,
            "The cat ____ lazily on the windowsill.",
            "straighten or extend one's body",
            "stretched",
            0,
            TEST_QUESTION_LIST_ID
        );
        questionRepository.addQuestion(question);
        var request = new FillInTheBlanksAttemptRequest(question.getID(), "stretched");

        AttemptResponse response = attemptQuestionUseCase.execute(TEST_USER_ID, request);

        assertNotNull(response);
        assertSame(AttemptStatus.Success, response.getAttemptStatus());
    }

    @Test
    void shouldReturnIncorrectResponseForInvalidAttempt() throws QuestionNotFoundException {
        var question = new FillInTheBlanksQuestion(
            UUID.randomUUID().toString(),
            Language.English,
            "The cat ____ lazily on the windowsill.",
            "straighten or extend one's body",
            "stretched",
            0,
            TEST_QUESTION_LIST_ID
        );
        questionRepository.addQuestion(question);
        var request = new FillInTheBlanksAttemptRequest(question.getID(), "wrong answer");

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
    void shouldUpdateReviewWithHighScoreOnSuccessfulAttempt() throws QuestionNotFoundException {
        var question = new FillInTheBlanksQuestion(
            UUID.randomUUID().toString(),
            Language.English,
            "The cat ____ lazily on the windowsill.",
            "straighten or extend one's body",
            "stretched",
            0,
            TEST_QUESTION_LIST_ID
        );
        questionRepository.addQuestion(question);
        var request = new FillInTheBlanksAttemptRequest(question.getID(), "stretched");
        var before = Instant.now();

        attemptQuestionUseCase.execute(TEST_USER_ID, request);

        var review = questionReviewRepository.getReviewForQuestionOrCreateNew(TEST_USER_ID, question);
        var diff = Duration.between(before, review.getNextReviewInstant());
        assertTrue(diff.toDays() >= 1);
    }

    @Test
    void shouldUpdateReviewWithLowScoreOnFailedAttempt() throws QuestionNotFoundException {
        var question = new FillInTheBlanksQuestion(
            UUID.randomUUID().toString(),
            Language.English,
            "The cat ____ lazily on the windowsill.",
            "straighten or extend one's body",
            "stretched",
            0,
            TEST_QUESTION_LIST_ID
        );
        questionRepository.addQuestion(question);
        var request = new FillInTheBlanksAttemptRequest(question.getID(), "wrong answer");
        var before = Instant.now();

        attemptQuestionUseCase.execute(TEST_USER_ID, request);

        var review = questionReviewRepository.getReviewForQuestionOrCreateNew(TEST_USER_ID, question);
        var diff = Duration.between(before, review.getNextReviewInstant());
        assertTrue(diff.toSeconds() <= 1);
    }
}
