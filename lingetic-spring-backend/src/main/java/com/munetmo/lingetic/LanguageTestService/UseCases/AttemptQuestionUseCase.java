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

    public AttemptResponse execute(String userId, AttemptRequest request) throws QuestionNotFoundException {
        var question = questionRepository.getQuestionByID(request.getQuestionID());
        var response = question.assessAttempt(request);

        // There's a race condition here: a review is fetched and then updated un-atomically. It's not a big deal
        // because the race condition will only be triggered if a particular user attempts the same question from
        // multiple devices at the same time. Moreover, the worst consequence would be a lost review, which is not a
        // big deal.
        var questionReview = questionReviewRepository.getReviewForQuestionOrCreateNew(userId, question);
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
