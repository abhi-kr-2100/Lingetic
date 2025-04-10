package com.munetmo.lingetic.LanguageTestService.UseCases;

import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptRequests.FillInTheBlanksAttemptRequest;
import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptResponses.AttemptResponse;
import com.munetmo.lingetic.LanguageTestService.Entities.AttemptStatus;
import com.munetmo.lingetic.LanguageTestService.Entities.Language;
import com.munetmo.lingetic.LanguageTestService.Entities.QuestionList;
import com.munetmo.lingetic.LanguageTestService.Exceptions.QuestionNotFoundException;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.FillInTheBlanksQuestion;
import com.munetmo.lingetic.LanguageTestService.infra.Repositories.Postgres.QuestionListPostgresRepository;
import com.munetmo.lingetic.LanguageTestService.infra.Repositories.Postgres.QuestionPostgresRepository;
import com.munetmo.lingetic.LanguageTestService.infra.Repositories.Postgres.QuestionReviewPostgresRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
class AttemptQuestionUseCaseTest {
    @Container
    @ServiceConnection
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private AttemptQuestionUseCase attemptQuestionUseCase;

    @Autowired
    private QuestionPostgresRepository questionRepository;

    @Autowired
    private QuestionReviewPostgresRepository questionReviewRepository;

    @Autowired
    private QuestionListPostgresRepository questionListRepository;

    private static final String TEST_USER_ID = UUID.randomUUID().toString();
    private static final String TEST_QUESTION_LIST_ID = UUID.randomUUID().toString();

    @BeforeEach
    void setUp() {
        questionReviewRepository.deleteAllReviews();
        questionRepository.deleteAllQuestions();
        questionListRepository.deleteAllQuestionLists();

        questionListRepository.addQuestionList(new QuestionList(
            TEST_QUESTION_LIST_ID,
            "Test Question List",
            Language.English
        ));

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
