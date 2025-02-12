package com.munetmo.lingetic.LanguageTestService.UseCases;

import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptRequests.AttemptRequest;
import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptResponses.AttemptResponse;
import com.munetmo.lingetic.LanguageTestService.Entities.AttemptStatus;
import com.munetmo.lingetic.LanguageTestService.Exceptions.QuestionNotFoundException;
import com.munetmo.lingetic.LanguageTestService.Repositories.QuestionRepository;
import com.munetmo.lingetic.LanguageTestService.Repositories.QuestionReviewRepository;

public class AttemptQuestionUseCase {
    private final QuestionRepository questionRepository;
    private final QuestionReviewRepository questionReviewRepository;

    public AttemptQuestionUseCase(QuestionRepository questionRepository, QuestionReviewRepository questionReviewRepository) {
        this.questionRepository = questionRepository;
        this.questionReviewRepository = questionReviewRepository;
    }

    public AttemptResponse execute(AttemptRequest request) throws QuestionNotFoundException {
        var question = questionRepository.getQuestionByID(request.getQuestionID());
        var response = question.assessAttempt(request);
        
        var questionReview = questionReviewRepository.getReviewByQuestionIDOrCreate(request.getQuestionID());
        questionReview.review(
            switch (response.getAttemptStatus()) {
                case AttemptStatus.Success -> 5;
                case AttemptStatus.Failure -> 0;
            }
        );
        questionReviewRepository.update(questionReview);
        
        return response;
    }
}
