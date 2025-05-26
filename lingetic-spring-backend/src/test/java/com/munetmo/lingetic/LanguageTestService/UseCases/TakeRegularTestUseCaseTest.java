package com.munetmo.lingetic.LanguageTestService.UseCases;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.IntStream;

import com.munetmo.lingetic.LanguageTestService.DTOs.Question.QuestionDTO;
import com.munetmo.lingetic.LanguageService.Entities.Language;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.FillInTheBlanksQuestion;
import com.munetmo.lingetic.LanguageTestService.infra.Repositories.Postgres.*;
import com.munetmo.lingetic.LanguageTestService.Entities.Sentence;
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
    private SentencePostgresRepository sentenceRepository;

    @Autowired
    private SentenceReviewPostgresRepository sentenceReviewRepository;

    private static final String TEST_USER_ID = UUID.randomUUID().toString();
    private static final String TEST_SENTENCE_ID = UUID.randomUUID().toString();

    @BeforeEach
    void setUp() {
        questionRepository.deleteAllQuestions();
        sentenceRepository.deleteAllSentences();

        sentenceRepository.addSentence(new Sentence(
                UUID.fromString(TEST_SENTENCE_ID),
                Language.English,
                "He walks to school.",
                Language.Turkish,
                "O okula yürür.",
                10,
                List.of()
        ));
    }

    private void addTestQuestions(int count) {
        IntStream.rangeClosed(1, count).forEach(i -> {
            var sentence = new Sentence(
                    UUID.randomUUID(),
                    Language.English,
                    "He walks to school.",
                    Language.Turkish,
                    "O okula yürür.",
                    10,
                    List.of()
            );
            sentenceRepository.addSentence(sentence);
            questionRepository.addQuestion(new FillInTheBlanksQuestion(
                    UUID.randomUUID().toString(),
                    Language.English,
                    "Question " + i + ": He ____ to school.",
                    "motion verb",
                    "walks",
                    sentence.id().toString(),
                    List.of()
            ));
        });
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

            var sentence = sentenceRepository.getSentenceByID(question.getSentenceID());
            var sentenceReview = sentenceReviewRepository.getReviewForSentenceOrCreateNew(TEST_USER_ID, sentence);
            sentenceReview.review(quality);
            sentenceReviewRepository.update(sentenceReview);

            --n;
        }
    }

    @Test
    void shouldReturnAllQuestionsWhenLessThanLimit() {
        sentenceRepository.deleteAllSentences();
        int questionsCount = TakeRegularTestUseCase.limit - 5;
        addTestQuestions(questionsCount);

        List<QuestionDTO> result = useCase.execute(TEST_USER_ID, Language.English);

        assertEquals(questionsCount, result.size());
        assertTrue(result.stream().allMatch(Objects::nonNull));
    }

    @Test
    void shouldReturnQuestionsUpToLimitWhenMoreQuestionsExist() {
        sentenceRepository.deleteAllSentences();
        int questionsCount = TakeRegularTestUseCase.limit + 5;
        addTestQuestions(questionsCount);

        List<QuestionDTO> result = useCase.execute(TEST_USER_ID, Language.English);

        assertEquals(TakeRegularTestUseCase.limit, result.size());
        assertTrue(result.stream().allMatch(Objects::nonNull));
    }

    @Test
    void shouldReturnEmptyListWhenNoQuestionsExist() {
        sentenceRepository.deleteAllSentences();
        List<QuestionDTO> result = useCase.execute(TEST_USER_ID, Language.English);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnNewQuestionsIfNoQuestionsToReview() {
        sentenceRepository.deleteAllSentences();
        addTestQuestions(TakeRegularTestUseCase.limit);

        var result = useCase.execute(TEST_USER_ID, Language.English);

        assertEquals(TakeRegularTestUseCase.limit, result.size());
    }

    @Test
    void shouldReturnQuestionsScheduledForReview() {
        addTestQuestions(TakeRegularTestUseCase.limit);
        var reviewedQuestion = new FillInTheBlanksQuestion(UUID.randomUUID().toString(), Language.English, "He ____ to school.", "motion verb", "walks", TEST_SENTENCE_ID, List.of());
        questionRepository.addQuestion(reviewedQuestion);

        var result = useCase.execute(TEST_USER_ID, Language.English);

        assertTrue(result.stream().anyMatch(q -> q.getSentenceID().equals(reviewedQuestion.getSentenceID())));
    }

    @Test
    void shouldNotReturnDuplicateQuestions() {
        sentenceRepository.deleteAllSentences();
        addTestQuestions(TakeRegularTestUseCase.limit);
        reviewNQuestions(TakeRegularTestUseCase.limit / 2, 1, 0);

        var result = useCase.execute(TEST_USER_ID, Language.English);

        assertEquals(TakeRegularTestUseCase.limit, result.stream().map(QuestionDTO::getSentenceID).distinct().count());
    }

    @Test
    void shouldNotReturnQuestionsScheduledForLaterIfNewerQuestionsExist() {
        sentenceRepository.deleteAllSentences();
        addTestQuestions(TakeRegularTestUseCase.limit * 2);
        reviewNQuestions(TakeRegularTestUseCase.limit / 2, 5, 0);
        var reviewedSentenceIDs = sentenceReviewRepository.getAllReviews(TEST_USER_ID)
                .stream()
                .map(r -> r.sentenceID)
                .toList();

        var result = useCase.execute(TEST_USER_ID, Language.English);

        assertFalse(result.stream().anyMatch(q -> reviewedSentenceIDs.contains(q.getSentenceID())));
    }

    @Test
    void shouldReturnQuestionsScheduledForLaterIfNoNewerQuestionsExist() {
        sentenceRepository.deleteAllSentences();
        addTestQuestions(TakeRegularTestUseCase.limit);
        reviewNQuestions(TakeRegularTestUseCase.limit, 5, 0);

        var result = useCase.execute(TEST_USER_ID, Language.English);

        assertEquals(TakeRegularTestUseCase.limit, result.size());
    }

    @Test
    void shouldReturnAMixOfScheduledNewAndReviewQuestionsIfNoneFulfillTheLimitAlone() {
        sentenceRepository.deleteAllSentences();
        addTestQuestions(TakeRegularTestUseCase.limit / 2);
        reviewNQuestions(1, 1, 0);
        reviewNQuestions(1, 5, 1);

        var result = useCase.execute(TEST_USER_ID, Language.English);

        assertEquals(TakeRegularTestUseCase.limit / 2, result.size());
    }

    @Test
    void shouldReturnQuestionsOrderedByDifficulty() {
        sentenceRepository.deleteAllSentences();

        var sentence1 = new Sentence(
                UUID.randomUUID(),
                Language.English,
                "He walks to school.",
                Language.Turkish,
                "O okula yürür.",
                10,
                List.of()
        );
        sentenceRepository.addSentence(sentence1);
        var question1 = new FillInTheBlanksQuestion(UUID.randomUUID().toString(), Language.English, "He ___ to school.", "motion verb", "walks", sentence1.id().toString(), List.of());

        var sentence2 = new Sentence(
                UUID.randomUUID(),
                Language.English,
                "She runs fast.",
                Language.Turkish,
                "O hızlı koşar.",
                20,
                List.of()
        );
        sentenceRepository.addSentence(sentence2);
        var question2 = new FillInTheBlanksQuestion(UUID.randomUUID().toString(), Language.English, "She ___ fast.", "motion verb", "runs", sentence2.id().toString(), List.of());

        var sentence3 = new Sentence(
                UUID.randomUUID(),
                Language.English,
                "They dance together.",
                Language.Turkish,
                "Onlar birlikte dans eder.",
                -10,
                List.of()
        );
        sentenceRepository.addSentence(sentence3);
        var question3 = new FillInTheBlanksQuestion(UUID.randomUUID().toString(), Language.English, "They ___ together.", "motion verb", "dance", sentence3.id().toString(), List.of());

        questionRepository.addQuestion(question1);
        questionRepository.addQuestion(question2);
        questionRepository.addQuestion(question3);

        List<QuestionDTO> result = useCase.execute(TEST_USER_ID, Language.English);

        assertEquals(3, result.size());
        assertEquals(question3.getSentenceID(), result.get(0).getSentenceID());
        assertEquals(question1.getSentenceID(), result.get(1).getSentenceID());
        assertEquals(question2.getSentenceID(), result.get(2).getSentenceID());
    }

    @Test
    void shouldOnlyOrderQuestionsByDifficultyIfTheyAreUnreviewed() {
        var question1 = new FillInTheBlanksQuestion(UUID.randomUUID().toString(), Language.English, "He ___ to school.", "motion verb", "walks", TEST_SENTENCE_ID, List.of());
        var question2 = new FillInTheBlanksQuestion(UUID.randomUUID().toString(), Language.English, "She ___ fast.", "motion verb", "runs", TEST_SENTENCE_ID, List.of());
        var question3 = new FillInTheBlanksQuestion(UUID.randomUUID().toString(), Language.English, "They ___ together.", "motion verb", "dance", TEST_SENTENCE_ID, List.of());
        var question4 = new FillInTheBlanksQuestion(UUID.randomUUID().toString(), Language.English, "I ___ to work.", "motion verb", "drive", TEST_SENTENCE_ID, List.of());
        var question5 = new FillInTheBlanksQuestion(UUID.randomUUID().toString(), Language.English, "We ___ home.", "motion verb", "walk", TEST_SENTENCE_ID, List.of());

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
            var id = q.getSentenceID();
            assertFalse(id.equals(question1.getSentenceID()) ||
                       id.equals(question2.getSentenceID()) ||
                       id.equals(question3.getSentenceID()) ||
                       id.equals(question4.getSentenceID()) ||
                       id.equals(question5.getSentenceID()));
        });
    }
}
