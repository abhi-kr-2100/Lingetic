package com.munetmo.lingetic.LanguageTestService.UseCases;

import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptRequests.FillInTheBlanksAttemptRequest;
import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptResponses.AttemptResponse;
import com.munetmo.lingetic.LanguageTestService.Entities.AttemptStatus;
import com.munetmo.lingetic.LanguageTestService.Exceptions.QuestionNotFoundException;
import com.munetmo.lingetic.LanguageTestService.infra.Repositories.InMemory.QuestionInMemoryRepository;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.FillInTheBlanksQuestion;
import com.munetmo.lingetic.LanguageTestService.infra.Repositories.InMemory.QuestionReviewInMemoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class AttemptQuestionUseCaseTest {
    private AttemptQuestionUseCase attemptQuestionUseCase;
    private QuestionInMemoryRepository questionRepository;
    private QuestionReviewInMemoryRepository questionReviewRepository;

    @BeforeEach
    void setUp() {
        questionReviewRepository = new QuestionReviewInMemoryRepository();
        questionRepository = new QuestionInMemoryRepository(questionReviewRepository);
        attemptQuestionUseCase = new AttemptQuestionUseCase(questionRepository, questionReviewRepository);

        questionRepository.addQuestion(new FillInTheBlanksQuestion(
            "1",
            "en",
            "The cat ____ lazily on the windowsill.",
            "straighten or extend one's body",
            "stretched"
        ));
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

        assertThrows(QuestionNotFoundException.class, () -> attemptQuestionUseCase.execute(request));
    }

    @Test
    void shouldUpdateReviewWithHighScoreOnSuccessfulAttempt() throws QuestionNotFoundException {
        var question = new FillInTheBlanksQuestion(
            "1",
            "en",
            "The cat ____ lazily on the windowsill.",
            "straighten or extend one's body",
            "stretched"
        );
        var request = new FillInTheBlanksAttemptRequest(question.getID(), "stretched");
        var before = Instant.now();

        attemptQuestionUseCase.execute(request);

        var review = questionReviewRepository.getReviewForQuestionOrCreateNew(question);
        var diff = Duration.between(before, review.getNextReviewInstant());
        assertTrue(diff.toDays() >= 1);
    }

    @Test
    void shouldUpdateReviewWithLowScoreOnFailedAttempt() throws QuestionNotFoundException {
        var question = new FillInTheBlanksQuestion(
            "1",
            "en",
            "The cat ____ lazily on the windowsill.",
            "straighten or extend one's body",
            "stretched"
        );
        var request = new FillInTheBlanksAttemptRequest(question.getID(), "wrong answer");
        var before = Instant.now();

        attemptQuestionUseCase.execute(request);

        var review = questionReviewRepository.getReviewForQuestionOrCreateNew(question);
        var diff = Duration.between(before, review.getNextReviewInstant());
        assertTrue(diff.toSeconds() <= 1);
    }
}
