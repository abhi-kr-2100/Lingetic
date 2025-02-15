package com.munetmo.lingetic.LanguageTestService.UseCases;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import com.munetmo.lingetic.LanguageTestService.Entities.Language;
import com.munetmo.lingetic.LanguageTestService.Repositories.QuestionRepository;
import com.munetmo.lingetic.LanguageTestService.infra.Repositories.InMemory.QuestionReviewInMemoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.munetmo.lingetic.LanguageTestService.DTOs.Question.QuestionDTO;
import com.munetmo.lingetic.LanguageTestService.infra.Repositories.InMemory.QuestionInMemoryRepository;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.FillInTheBlanksQuestion;

class TakeRegularTestUseCaseTest {
    private TakeRegularTestUseCase useCase;
    private QuestionRepository questionRepository;
    private QuestionReviewInMemoryRepository questionReviewRepository;

    @BeforeEach
    void setUp() {
        questionReviewRepository = new QuestionReviewInMemoryRepository();
        questionRepository = new QuestionInMemoryRepository(questionReviewRepository);
        useCase = new TakeRegularTestUseCase(questionRepository, questionReviewRepository);
    }

    private void addTestQuestions(int count) {
        IntStream.rangeClosed(1, count).forEach(i -> questionRepository.addQuestion(new FillInTheBlanksQuestion(
                String.valueOf(i),
                Language.English,
                "Question " + i + ": He ____ to school.",
                "motion verb",
                "walks"
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

            var questionReview = questionReviewRepository.getReviewForQuestionOrCreateNew(question);
            questionReview.review(quality);
            questionReviewRepository.update(questionReview);

            --n;
        }
    }

    @Test
    void shouldReturnAllQuestionsWhenLessThanLimit() {
        int questionsCount = TakeRegularTestUseCase.limit - 5;
        addTestQuestions(questionsCount);

        List<QuestionDTO> result = useCase.execute(Language.English);

        assertEquals(questionsCount, result.size());
        assertTrue(result.stream().allMatch(Objects::nonNull));
    }

    @Test
    void shouldReturnQuestionsUpToLimitWhenMoreQuestionsExist() {
        int questionsCount = TakeRegularTestUseCase.limit + 5;
        addTestQuestions(questionsCount);

        List<QuestionDTO> result = useCase.execute(Language.English);

        assertEquals(TakeRegularTestUseCase.limit, result.size());
        assertTrue(result.stream().allMatch(Objects::nonNull));
    }

    @Test
    void shouldReturnEmptyListWhenNoQuestionsExist() {
        List<QuestionDTO> result = useCase.execute(Language.English);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldOnlyReturnQuestionsInRequestedLanguage() {
        questionRepository.addQuestion(new FillInTheBlanksQuestion("1", Language.English, "He ____ to school.", "motion verb", "walks"));
        questionRepository.addQuestion(new FillInTheBlanksQuestion("2", Language.DummyLanguage, "El ____ a la escuela.", "verbo de movimiento", "camina"));
        questionRepository.addQuestion(new FillInTheBlanksQuestion("3", Language.English, "She ____ fast.", "motion verb", "runs"));
        questionRepository.addQuestion(new FillInTheBlanksQuestion("4", Language.DummyLanguage, "Il ____ à l'école.", "verbe de mouvement", "marche"));

        List<QuestionDTO> result = useCase.execute(Language.English);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(q -> q.getLanguage().equals(Language.English)));
    }

    @Test
    void shouldReturnNewQuestionsIfNoQuestionsToReview() {
        addTestQuestions(TakeRegularTestUseCase.limit);

        var result = useCase.execute(Language.English);

        assertEquals(TakeRegularTestUseCase.limit, result.size());
    }

    @Test
    void shouldReturnQuestionsScheduledForReview() {
        addTestQuestions(TakeRegularTestUseCase.limit);
        var reviewedQuestion = new FillInTheBlanksQuestion("rq1", Language.English, "He ____ to school.", "motion verb", "walks");
        questionRepository.addQuestion(reviewedQuestion);
        var questionReview = questionReviewRepository.getReviewForQuestionOrCreateNew(reviewedQuestion);
        questionReview.review(1);
        questionReviewRepository.update(questionReview);

        var result = useCase.execute(Language.English);

        assertTrue(result.stream().anyMatch(q -> q.getID().equals(reviewedQuestion.getID())));
    }

    @Test
    void shouldNotReturnDuplicateQuestions() {
        addTestQuestions(TakeRegularTestUseCase.limit);
        reviewNQuestions(TakeRegularTestUseCase.limit / 2, 1, 0);

        var result = useCase.execute(Language.English);

        assertEquals(TakeRegularTestUseCase.limit, result.stream().map(QuestionDTO::getID).distinct().count());
    }

    @Test
    void shouldNotReturnQuestionsScheduledForLaterIfNewerQuestionsExist() {
        addTestQuestions(TakeRegularTestUseCase.limit * 2);
        reviewNQuestions(TakeRegularTestUseCase.limit / 2, 5, 0);
        var reviewedQuestionIDs = questionReviewRepository.getAllReviews()
                .stream()
                .map(r -> r.questionID)
                .toList();

        var result = useCase.execute(Language.English);

        assertFalse(result.stream().anyMatch(q -> reviewedQuestionIDs.contains(q.getID())));
    }

    @Test
    void shouldReturnQuestionsScheduledForLaterIfNoNewerQuestionsExist() {
        addTestQuestions(TakeRegularTestUseCase.limit);
        reviewNQuestions(TakeRegularTestUseCase.limit, 5, 0);

        var result = useCase.execute(Language.English);

        assertEquals(TakeRegularTestUseCase.limit, result.size());
    }

    @Test
    void shouldReturnAMixOfScheduledNewAndReviewQuestionsIfNoneFulfillTheLimitAlone() {
        addTestQuestions(TakeRegularTestUseCase.limit / 2);
        reviewNQuestions(1, 1, 0);
        reviewNQuestions(1, 5, 1);

        var result = useCase.execute(Language.English);

        assertEquals(TakeRegularTestUseCase.limit / 2, result.size());
    }
}
