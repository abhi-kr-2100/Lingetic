package com.munetmo.lingetic.LanguageTestService.UseCases;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.IntStream;

import com.munetmo.lingetic.LanguageTestService.DTOs.Question.QuestionDTO;
import com.munetmo.lingetic.LanguageTestService.Entities.Language;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.FillInTheBlanksQuestion;
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

@SpringBootTest
@Testcontainers
class TakeRegularTestUseCaseTest {
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
    private TakeRegularTestUseCase useCase;

    @Autowired
    private QuestionPostgresRepository questionRepository;

    @Autowired
    private QuestionReviewPostgresRepository questionReviewRepository;

    private static final String TEST_USER_ID = UUID.randomUUID().toString();
    private static final String TEST_QUESTION_LIST_ID = UUID.randomUUID().toString();

    @BeforeEach
    void setUp() {
        questionReviewRepository.deleteAllReviews();
        questionRepository.deleteAllQuestions();
    }

    private void addTestQuestions(int count) {
        IntStream.rangeClosed(1, count).forEach(i -> questionRepository.addQuestion(new FillInTheBlanksQuestion(
                UUID.randomUUID().toString(),
                Language.English,
                "Question " + i + ": He ____ to school.",
                "motion verb",
                "walks",
                0,
                TEST_QUESTION_LIST_ID
        )));
    }

    private void reviewNQuestions(int n, int quality, int startIdx) {
        var questions = questionRepository.getAllQuestions();
        for (var question : questions) {
            if (startIdx > 0) {
                --startIdx;
                continue;
            }

            if (n <= 0) {
                break;
            }

            var questionReview = questionReviewRepository.getReviewForQuestionOrCreateNew(TEST_USER_ID, question);
            questionReview.review(quality);
            questionReviewRepository.update(questionReview);

            --n;
        }
    }

    @Test
    void shouldReturnAllQuestionsWhenLessThanLimit() {
        int questionsCount = TakeRegularTestUseCase.limit - 5;
        addTestQuestions(questionsCount);

        List<QuestionDTO> result = useCase.execute(TEST_USER_ID, Language.English);

        assertEquals(questionsCount, result.size());
        assertTrue(result.stream().allMatch(Objects::nonNull));
    }

    @Test
    void shouldReturnQuestionsUpToLimitWhenMoreQuestionsExist() {
        int questionsCount = TakeRegularTestUseCase.limit + 5;
        addTestQuestions(questionsCount);

        List<QuestionDTO> result = useCase.execute(TEST_USER_ID, Language.English);

        assertEquals(TakeRegularTestUseCase.limit, result.size());
        assertTrue(result.stream().allMatch(Objects::nonNull));
    }

    @Test
    void shouldReturnEmptyListWhenNoQuestionsExist() {
        List<QuestionDTO> result = useCase.execute(TEST_USER_ID, Language.English);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldOnlyReturnQuestionsInRequestedLanguage() {
        questionRepository.addQuestion(new FillInTheBlanksQuestion(UUID.randomUUID().toString(), Language.English, "He ____ to school.", "motion verb", "walks", 0, TEST_QUESTION_LIST_ID));
        questionRepository.addQuestion(new FillInTheBlanksQuestion(UUID.randomUUID().toString(), Language.Turkish, "El ____ a la escuela.", "verbo de movimiento", "camina", 0, TEST_QUESTION_LIST_ID));
        questionRepository.addQuestion(new FillInTheBlanksQuestion(UUID.randomUUID().toString(), Language.English, "She ____ fast.", "motion verb", "runs", 0, TEST_QUESTION_LIST_ID));
        questionRepository.addQuestion(new FillInTheBlanksQuestion(UUID.randomUUID().toString(), Language.Turkish, "Il ____ à l'école.", "verbe de mouvement", "marche", 0, TEST_QUESTION_LIST_ID));

        List<QuestionDTO> result = useCase.execute(TEST_USER_ID, Language.English);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(q -> q.getLanguage().equals(Language.English)));
    }

    @Test
    void shouldReturnNewQuestionsIfNoQuestionsToReview() {
        addTestQuestions(TakeRegularTestUseCase.limit);

        var result = useCase.execute(TEST_USER_ID, Language.English);

        assertEquals(TakeRegularTestUseCase.limit, result.size());
    }

    @Test
    void shouldReturnQuestionsScheduledForReview() {
        addTestQuestions(TakeRegularTestUseCase.limit);
        var reviewedQuestion = new FillInTheBlanksQuestion(UUID.randomUUID().toString(), Language.English, "He ____ to school.", "motion verb", "walks", 0, TEST_QUESTION_LIST_ID);
        questionRepository.addQuestion(reviewedQuestion);
        var questionReview = questionReviewRepository.getReviewForQuestionOrCreateNew(TEST_USER_ID, reviewedQuestion);
        questionReview.review(1);
        questionReviewRepository.update(questionReview);

        var result = useCase.execute(TEST_USER_ID, Language.English);

        assertTrue(result.stream().anyMatch(q -> q.getID().equals(reviewedQuestion.getID())));
    }

    @Test
    void shouldNotReturnDuplicateQuestions() {
        addTestQuestions(TakeRegularTestUseCase.limit);
        reviewNQuestions(TakeRegularTestUseCase.limit / 2, 1, 0);

        var result = useCase.execute(TEST_USER_ID, Language.English);

        assertEquals(TakeRegularTestUseCase.limit, result.stream().map(QuestionDTO::getID).distinct().count());
    }

    @Test
    void shouldNotReturnQuestionsScheduledForLaterIfNewerQuestionsExist() {
        addTestQuestions(TakeRegularTestUseCase.limit * 2);
        reviewNQuestions(TakeRegularTestUseCase.limit / 2, 5, 0);
        var reviewedQuestionIDs = questionReviewRepository.getAllReviews(TEST_USER_ID)
                .stream()
                .map(r -> r.questionID)
                .toList();

        var result = useCase.execute(TEST_USER_ID, Language.English);

        assertFalse(result.stream().anyMatch(q -> reviewedQuestionIDs.contains(q.getID())));
    }

    @Test
    void shouldReturnQuestionsScheduledForLaterIfNoNewerQuestionsExist() {
        addTestQuestions(TakeRegularTestUseCase.limit);
        reviewNQuestions(TakeRegularTestUseCase.limit, 5, 0);

        var result = useCase.execute(TEST_USER_ID, Language.English);

        assertEquals(TakeRegularTestUseCase.limit, result.size());
    }

    @Test
    void shouldReturnAMixOfScheduledNewAndReviewQuestionsIfNoneFulfillTheLimitAlone() {
        addTestQuestions(TakeRegularTestUseCase.limit / 2);
        reviewNQuestions(1, 1, 0);
        reviewNQuestions(1, 5, 1);

        var result = useCase.execute(TEST_USER_ID, Language.English);

        assertEquals(TakeRegularTestUseCase.limit / 2, result.size());
    }

    @Test
    void shouldReturnQuestionsOrderedByDifficulty() {
        var question1 = new FillInTheBlanksQuestion(UUID.randomUUID().toString(), Language.English, "He ___ to school.", "motion verb", "walks", 3, TEST_QUESTION_LIST_ID);
        var question2 = new FillInTheBlanksQuestion(UUID.randomUUID().toString(), Language.English, "She ___ fast.", "motion verb", "runs", 1, TEST_QUESTION_LIST_ID);
        var question3 = new FillInTheBlanksQuestion(UUID.randomUUID().toString(), Language.English, "They ___ together.", "motion verb", "dance", 2, TEST_QUESTION_LIST_ID);

        questionRepository.addQuestion(question1);
        questionRepository.addQuestion(question2);
        questionRepository.addQuestion(question3);

        List<QuestionDTO> result = useCase.execute(TEST_USER_ID, Language.English);

        assertEquals(3, result.size());
        assertEquals(question2.getID(), result.get(0).getID());
        assertEquals(question3.getID(), result.get(1).getID());
        assertEquals(question1.getID(), result.get(2).getID());
    }

    @Test
    void shouldOnlyOrderQuestionsByDifficultyIfTheyAreUnreviewed() {
        var question1 = new FillInTheBlanksQuestion(UUID.randomUUID().toString(), Language.English, "He ___ to school.", "motion verb", "walks", 10, TEST_QUESTION_LIST_ID);
        var question2 = new FillInTheBlanksQuestion(UUID.randomUUID().toString(), Language.English, "She ___ fast.", "motion verb", "runs", 5, TEST_QUESTION_LIST_ID);
        var question3 = new FillInTheBlanksQuestion(UUID.randomUUID().toString(), Language.English, "They ___ together.", "motion verb", "dance", 8, TEST_QUESTION_LIST_ID);
        var question4 = new FillInTheBlanksQuestion(UUID.randomUUID().toString(), Language.English, "I ___ to work.", "motion verb", "drive", 4, TEST_QUESTION_LIST_ID);
        var question5 = new FillInTheBlanksQuestion(UUID.randomUUID().toString(), Language.English, "We ___ home.", "motion verb", "walk", 1, TEST_QUESTION_LIST_ID);

        questionRepository.addQuestion(question1);
        questionRepository.addQuestion(question2);
        questionRepository.addQuestion(question3);
        questionRepository.addQuestion(question4);
        questionRepository.addQuestion(question5);

        addTestQuestions(10); // easy questions; these should be returned over the more difficult unreviewed questions
        reviewNQuestions(3, 1, 0);

        var result = useCase.execute(TEST_USER_ID, Language.English);
        var unreviewedQuestions = result.subList(3, result.size());

        unreviewedQuestions.forEach(q -> {
            var id = q.getID();
            assertFalse(id.equals(question1.getID()) ||
                       id.equals(question2.getID()) ||
                       id.equals(question3.getID()) ||
                       id.equals(question4.getID()) ||
                       id.equals(question5.getID()));
        });
    }
}
