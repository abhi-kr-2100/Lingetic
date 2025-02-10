package com.munetmo.lingetic.LanguageTestService.UseCases;

import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptRequests.AttemptRequest;
import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptResponses.AttemptResponse;
import com.munetmo.lingetic.LanguageTestService.Exceptions.QuestionNotFoundException;
import com.munetmo.lingetic.LanguageTestService.Repositories.QuestionRepository;

public class AttemptQuestionUseCase {
    private QuestionRepository questionRepository;

    public AttemptQuestionUseCase(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    public AttemptResponse execute(AttemptRequest request) throws QuestionNotFoundException {
        var question = questionRepository.getQuestionByID(request.getQuestionID());
        return question.assessAttempt(request);
    }
}
