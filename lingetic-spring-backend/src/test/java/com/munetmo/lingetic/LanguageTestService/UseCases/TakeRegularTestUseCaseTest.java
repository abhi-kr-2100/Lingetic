package com.munetmo.lingetic.LanguageTestService.UseCases;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.munetmo.lingetic.LanguageTestService.DTOs.Question.QuestionDTO;
import com.munetmo.lingetic.LanguageTestService.infra.Repositories.InMemory.QuestionInMemoryRepository;

class TakeRegularTestUseCaseTest {
    private TakeRegularTestUseCase useCase;

    @BeforeEach
    void setUp() {
        QuestionInMemoryRepository repository = new QuestionInMemoryRepository();
        useCase = new TakeRegularTestUseCase(repository);
    }

    @Test
    void shouldReturnAllQuestionsAsQuestionDTOs() {
        List<QuestionDTO> result = useCase.execute();

        assertTrue(result.size() <= TakeRegularTestUseCase.limit);
        assertTrue(result.stream().allMatch(Objects::nonNull));
    }
}
