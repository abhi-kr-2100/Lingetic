package com.munetmo.lingetic.LanguageTestService.UseCases;

import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptRequests.AttemptRequest;
import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptRequests.FillInTheBlanksAttemptRequest;
import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptResponses.AttemptResponse;
import com.munetmo.lingetic.LanguageTestService.Entities.AttemptStatus;
import com.munetmo.lingetic.LanguageTestService.Exceptions.QuestionNotFoundException;
import com.munetmo.lingetic.LanguageTestService.infra.Repositories.InMemory.QuestionInMemoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AttemptQuestionUseCaseTest {
    private AttemptQuestionUseCase attemptQuestionUseCase;

    @BeforeEach
    void setUp() {
        var questionRepository = new QuestionInMemoryRepository();
        attemptQuestionUseCase = new AttemptQuestionUseCase(questionRepository);
    }

    @Test
    void shouldReturnCorrectResponseForValidAttempt() throws QuestionNotFoundException {
        var questionId = "1";
        var request = new FillInTheBlanksAttemptRequest(questionId, "stretched");

        AttemptResponse response = attemptQuestionUseCase.execute(request);

        assertNotNull(response);
        assertSame(AttemptStatus.Success, response.getAttemptStatus());
    }

    @Test
    void shouldReturnIncorrectResponseForInvalidAttempt() throws QuestionNotFoundException {
        var questionId = "1";
        var request = new FillInTheBlanksAttemptRequest(questionId, "wrong answer");

        AttemptResponse response = attemptQuestionUseCase.execute(request);

        assertNotNull(response);
        assertSame(AttemptStatus.Failure, response.getAttemptStatus());
    }

    @Test
    void shouldThrowQuestionNotFoundExceptionForNonexistentQuestion() {
        var questionId = "999";
        var request = new FillInTheBlanksAttemptRequest(questionId, "test answer");

        assertThrows(QuestionNotFoundException.class, () -> {
            attemptQuestionUseCase.execute(request);
        });
    }

    @Test
    void shouldThrowIllegalArgumentExceptionForNullRequest() {
        assertThrows(IllegalArgumentException.class, () -> {
            attemptQuestionUseCase.execute(null);
        });
    }
}
