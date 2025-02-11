package com.munetmo.lingetic.LanguageTestService.UseCases;

import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptRequests.AttemptRequest;
import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptResponses.AttemptResponse;
import com.munetmo.lingetic.LanguageTestService.Entities.AttemptStatus;
import com.munetmo.lingetic.LanguageTestService.Exceptions.QuestionNotFoundException;
import com.munetmo.lingetic.LanguageTestService.Repositories.QuestionRepository;
import com.munetmo.lingetic.LanguageTestService.Repositories.QuestionToReviewRepository;

public class AttemptQuestionUseCase {
    private final QuestionRepository questionRepository;
    private final QuestionToReviewRepository questionToReviewRepository;

    public AttemptQuestionUseCase(QuestionRepository questionRepository, QuestionToReviewRepository questionToReviewRepository) {
        this.questionRepository = questionRepository;
        this.questionToReviewRepository = questionToReviewRepository;
    }

    public AttemptResponse execute(AttemptRequest request) throws QuestionNotFoundException {
        var question = questionRepository.getQuestionByID(request.getQuestionID());
        var response = question.assessAttempt(request);
        
        var questionToReview = questionToReviewRepository.getReviewByQuestionIDOrCreate(request.getQuestionID());
        questionToReview.review(
            switch (response.getAttemptStatus()) {
                case AttemptStatus.Success -> 5;
                case AttemptStatus.Failure -> 0;
            }
        );
        questionToReviewRepository.update(questionToReview);
        
        return response;
    }
}
