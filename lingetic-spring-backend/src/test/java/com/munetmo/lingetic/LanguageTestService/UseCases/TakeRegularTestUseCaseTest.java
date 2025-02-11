package com.munetmo.lingetic.LanguageTestService.UseCases;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.munetmo.lingetic.LanguageTestService.DTOs.Question.QuestionDTO;
import com.munetmo.lingetic.LanguageTestService.infra.Repositories.InMemory.QuestionInMemoryRepository;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.FillInTheBlanksQuestion;

class TakeRegularTestUseCaseTest {
    private TakeRegularTestUseCase useCase;
    private QuestionInMemoryRepository repository;

    @BeforeEach
    void setUp() {
        repository = new QuestionInMemoryRepository();
        useCase = new TakeRegularTestUseCase(repository);
    }

    private void addTestQuestions(int count) {
        IntStream.rangeClosed(1, count).forEach(i -> repository.addQuestion(new FillInTheBlanksQuestion(
                String.valueOf(i),
                "en",
                "Question " + i + ": He ____ to school.",
                "motion verb",
                "walks"
        )));
    }

    @Test
    void shouldReturnAllQuestionsWhenLessThanLimit() {
        int questionsCount = TakeRegularTestUseCase.limit - 5;
        addTestQuestions(questionsCount);

        List<QuestionDTO> result = useCase.execute();

        assertEquals(questionsCount, result.size());
        assertTrue(result.stream().allMatch(Objects::nonNull));
    }

    @Test
    void shouldReturnQuestionsUpToLimitWhenMoreQuestionsExist() {
        int questionsCount = TakeRegularTestUseCase.limit + 5;
        addTestQuestions(questionsCount);

        List<QuestionDTO> result = useCase.execute();

        assertEquals(TakeRegularTestUseCase.limit, result.size());
        assertTrue(result.stream().allMatch(Objects::nonNull));
    }

    @Test
    void shouldReturnEmptyListWhenNoQuestionsExist() {
        List<QuestionDTO> result = useCase.execute();

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldOnlyReturnQuestionsInRequestedLanguage() {
        repository.addQuestion(new FillInTheBlanksQuestion("1", "en", "He ____ to school.", "motion verb", "walks"));
        repository.addQuestion(new FillInTheBlanksQuestion("2", "es", "El ____ a la escuela.", "verbo de movimiento", "camina"));
        repository.addQuestion(new FillInTheBlanksQuestion("3", "en", "She ____ fast.", "motion verb", "runs"));
        repository.addQuestion(new FillInTheBlanksQuestion("4", "fr", "Il ____ à l'école.", "verbe de mouvement", "marche"));

        List<QuestionDTO> result = useCase.execute("en");

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(q -> q.getLanguage().equals("en")));
    }
}
