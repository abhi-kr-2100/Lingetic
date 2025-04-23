package com.munetmo.lingetic.LanguageTestService.UseCases;

import com.munetmo.lingetic.LanguageTestService.DTOs.TaskPayloads.QuestionReviewProcessingPayload;
import com.munetmo.lingetic.LanguageTestService.Entities.AttemptStatus;
import com.munetmo.lingetic.LanguageService.Entities.Language;
import com.munetmo.lingetic.LanguageTestService.Entities.QuestionList;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.FillInTheBlanksQuestion;
import com.munetmo.lingetic.LanguageTestService.Exceptions.QuestionNotFoundException;
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

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
class ReviewQuestionUseCaseTest {

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
    private QuestionPostgresRepository questionRepository;

    @Autowired
    private QuestionReviewPostgresRepository questionReviewRepository;

    @Autowired
    private QuestionListPostgresRepository questionListRepository;

    private ReviewQuestionUseCase reviewQuestionUseCase;

    private static final String TEST_USER_ID = UUID.randomUUID().toString();
    private static final String TEST_QUESTION_LIST_ID = UUID.randomUUID().toString();
    private static final String TEST_QUESTION_ID = UUID.randomUUID().toString();

    @BeforeEach
    void setUp() {
        questionReviewRepository.deleteAllReviews();
        questionRepository.deleteAllQuestions();
        questionListRepository.deleteAllQuestionLists();

        questionListRepository.addQuestionList(
                new QuestionList(TEST_QUESTION_LIST_ID, "Test Question List", Language.English));

        var question = new FillInTheBlanksQuestion(
                TEST_QUESTION_ID,
                Language.English,
                "The dog ____ the ball.",
                "an action verb",
                "chased",
                0,
                TEST_QUESTION_LIST_ID);
        questionRepository.addQuestion(question);

        reviewQuestionUseCase = new ReviewQuestionUseCase(questionRepository, questionReviewRepository);
    }

    @Test
    void shouldReviewWithQualityFiveOnSuccessStatus() {
        var payload = new QuestionReviewProcessingPayload(TEST_USER_ID, TEST_QUESTION_ID, AttemptStatus.Success);
        reviewQuestionUseCase.execute(payload);

        var reviews = questionReviewRepository.getAllReviews(TEST_USER_ID);
        assertEquals(1, reviews.size());

        var review = reviews.getFirst();

        assertEquals(TEST_USER_ID, review.userID);
        assertEquals(TEST_QUESTION_ID, review.questionID);
        assertTrue(review.getNextReviewInstant().isAfter(Instant.now()));
    }

    @Test
    void shouldReviewWithQualityZeroOnFailureStatus() throws QuestionNotFoundException {
        var payload = new QuestionReviewProcessingPayload(TEST_USER_ID, TEST_QUESTION_ID, AttemptStatus.Failure);
        reviewQuestionUseCase.execute(payload);

        var reviews = questionReviewRepository.getAllReviews(TEST_USER_ID);
        assertEquals(1, reviews.size());

        var review = reviews.getFirst();

        assertEquals(TEST_USER_ID, review.userID);
        assertEquals(TEST_QUESTION_ID, review.questionID);
        assertTrue(review.getNextReviewInstant().isBefore(Instant.now().plusSeconds(1)));
    }
}
