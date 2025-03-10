package com.munetmo.lingetic.LanguageTestService.infra.Repositories.Postgres;

import com.munetmo.lingetic.LanguageTestService.Entities.Language;
import com.munetmo.lingetic.LanguageTestService.Entities.QuestionReview;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.FillInTheBlanksQuestion;
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
public class QuestionReviewPostgresRepositoryTest {
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");
    private static final String TEST_QUESTION_LIST_ID = UUID.randomUUID().toString();
    private static final Duration INSTANT_COMPARISON_TOLERANCE = Duration.ofMinutes(1);
    private static QuestionReviewPostgresRepository questionReviewRepository;
    private static QuestionPostgresRepository questionRepository;

    @BeforeAll
    static void beforeAll() {
        postgres.start();

        var dataSource = DataSourceBuilder.create()
                .url(postgres.getJdbcUrl())
                .username(postgres.getUsername())
                .password(postgres.getPassword())
                .build();
        var jdbcTemplate = new JdbcTemplate(dataSource);

        questionReviewRepository = new QuestionReviewPostgresRepository(jdbcTemplate);
        questionRepository = new QuestionPostgresRepository(jdbcTemplate);

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

        // Flyway is manually run in beforeAll; this overrides the value in application.properties
        // because Flyway being enabled in application.properties would cause the test to fail
        registry.add("spring.flyway.enabled", () -> false);
    }

    @BeforeEach
    void setUp() {
        questionReviewRepository.deleteAllReviews();
        questionRepository.deleteAllQuestions();
    }

    private FillInTheBlanksQuestion createAndAddQuestion(Language language) {
        var question = new FillInTheBlanksQuestion(
            UUID.randomUUID().toString(),
            language,
            "The cat ____ lazily.",
            "hint",
            "sleeps",
            0,
            TEST_QUESTION_LIST_ID
        );
        questionRepository.addQuestion(question);
        return question;
    }

    private boolean instantsAreEqual(Instant a, Instant b) {
        return Math.abs(Duration.between(a, b).toSeconds()) < INSTANT_COMPARISON_TOLERANCE.toSeconds();
    }

    @Test
    void shouldAddAndRetrieveReview() {
        var userId = UUID.randomUUID().toString();
        var question = createAndAddQuestion(Language.English);
        var review = new QuestionReview(
            UUID.randomUUID().toString(),
            question.getID(),
            userId,
            Language.English
        );

        questionReviewRepository.addReview(review);
        var reviews = questionReviewRepository.getAllReviews(userId);

        assertEquals(1, reviews.size());
        var retrievedReview = reviews.getFirst();
        assertEquals(review.id, retrievedReview.id);
        assertEquals(review.questionID, retrievedReview.questionID);
        assertEquals(review.userID, retrievedReview.userID);
        assertEquals(review.language, retrievedReview.language);
        assertEquals(review.getRepetitions(), retrievedReview.getRepetitions());
        assertEquals(review.getEaseFactor(), retrievedReview.getEaseFactor());
        assertEquals(review.getInterval(), retrievedReview.getInterval());
        assertTrue(instantsAreEqual(review.getNextReviewInstant(), retrievedReview.getNextReviewInstant()));
    }

    @Test
    void shouldGetTopQuestionsToReview() {
        var userId = UUID.randomUUID().toString();
        var question1 = createAndAddQuestion(Language.English);
        var question2 = createAndAddQuestion(Language.English);

        var review1 = new QuestionReview(
            UUID.randomUUID().toString(),
            question1.getID(),
            userId,
            Language.English
        );
        var review2 = new QuestionReview(
            UUID.randomUUID().toString(),
            question2.getID(),
            userId,
            Language.English
        );

        // Set different review times
        review1.review(5); // Will set a later review time
        review2.review(2); // Will set an earlier review time

        questionReviewRepository.addReview(review1);
        questionReviewRepository.addReview(review2);

        var topReviews = questionReviewRepository.getTopQuestionsToReview(userId, Language.English, 2);
        assertEquals(2, topReviews.size());
        // review2 should come first as it has an earlier next review time
        assertEquals(review2.id, topReviews.get(0).id);
        assertEquals(review1.id, topReviews.get(1).id);
    }

    @Test
    void shouldRespectLimitInTopQuestionsToReview() {
        var userId = UUID.randomUUID().toString();
        for (int i = 0; i < 5; i++) {
            var question = createAndAddQuestion(Language.English);
            var review = new QuestionReview(
                UUID.randomUUID().toString(),
                question.getID(),
                userId,
                Language.English
            );
            review.review(3);
            questionReviewRepository.addReview(review);
        }

        var topReviews = questionReviewRepository.getTopQuestionsToReview(userId, Language.English, 3);
        assertEquals(3, topReviews.size());
    }

    @Test
    void shouldUpdateExistingReview() {
        var userId = UUID.randomUUID().toString();
        var question = createAndAddQuestion(Language.English);
        var review = new QuestionReview(
            UUID.randomUUID().toString(),
            question.getID(),
            userId,
            Language.English
        );

        questionReviewRepository.addReview(review);
        
        // Modify the review
        review.review(5);
        var originalNextReview = review.getNextReviewInstant();
        questionReviewRepository.update(review);

        var reviews = questionReviewRepository.getAllReviews(userId);
        assertEquals(1, reviews.size());
        var updatedReview = reviews.getFirst();
        assertEquals(review.getRepetitions(), updatedReview.getRepetitions());
        assertEquals(review.getEaseFactor(), updatedReview.getEaseFactor());
        assertEquals(review.getInterval(), updatedReview.getInterval());
        assertTrue(instantsAreEqual(originalNextReview, updatedReview.getNextReviewInstant()));
    }

    @Test
    void shouldCreateNewReviewForQuestion() {
        var userId = UUID.randomUUID().toString();
        var question = createAndAddQuestion(Language.English);

        var review = questionReviewRepository.getReviewForQuestionOrCreateNew(userId, question);

        assertNotNull(review);
        assertEquals(question.getID(), review.questionID);
        assertEquals(userId, review.userID);
        assertEquals(question.getLanguage(), review.language);
    }

    @Test
    void shouldReturnExistingReviewForQuestion() {
        var userId = UUID.randomUUID().toString();
        var question = createAndAddQuestion(Language.English);
        
        // Create initial review
        var initialReview = questionReviewRepository.getReviewForQuestionOrCreateNew(userId, question);
        initialReview.review(5);
        questionReviewRepository.update(initialReview);

        // Get the same review again
        var retrievedReview = questionReviewRepository.getReviewForQuestionOrCreateNew(userId, question);

        assertEquals(initialReview.id, retrievedReview.id);
        assertEquals(initialReview.getRepetitions(), retrievedReview.getRepetitions());
        assertEquals(initialReview.getEaseFactor(), retrievedReview.getEaseFactor());
        assertEquals(initialReview.getInterval(), retrievedReview.getInterval());
        assertTrue(instantsAreEqual(initialReview.getNextReviewInstant(), retrievedReview.getNextReviewInstant()));
    }

    @Test
    void shouldFilterReviewsByLanguage() {
        var userId = UUID.randomUUID().toString();
        var englishQuestion = createAndAddQuestion(Language.English);
        var turkishQuestion = createAndAddQuestion(Language.Turkish);

        var englishReview = new QuestionReview(
            UUID.randomUUID().toString(),
            englishQuestion.getID(),
            userId,
            Language.English
        );
        var turkishReview = new QuestionReview(
            UUID.randomUUID().toString(),
            turkishQuestion.getID(),
            userId,
            Language.Turkish
        );

        questionReviewRepository.addReview(englishReview);
        questionReviewRepository.addReview(turkishReview);

        var englishReviews = questionReviewRepository.getTopQuestionsToReview(userId, Language.English, 10);
        var turkishReviews = questionReviewRepository.getTopQuestionsToReview(userId, Language.Turkish, 10);

        assertEquals(1, englishReviews.size());
        assertEquals(1, turkishReviews.size());
        assertEquals(englishReview.id, englishReviews.getFirst().id);
        assertEquals(turkishReview.id, turkishReviews.getFirst().id);
    }
}