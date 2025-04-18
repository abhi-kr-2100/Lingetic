package com.munetmo.lingetic.LanguageTestService.UseCases;

import com.munetmo.lingetic.LanguageTestService.DTOs.TaskPayloads.QuestionReviewProcessingPayload;
import com.munetmo.lingetic.LanguageTestService.Entities.AttemptStatus;
import com.munetmo.lingetic.LanguageTestService.Repositories.QuestionRepository;
import com.munetmo.lingetic.LanguageTestService.Repositories.QuestionReviewRepository;

public class ReviewQuestionUseCase {
    private final QuestionRepository questionRepository;
    private final QuestionReviewRepository questionReviewRepository;

    public ReviewQuestionUseCase(QuestionRepository questionRepository,
            QuestionReviewRepository questionReviewRepository) {
        this.questionRepository = questionRepository;
        this.questionReviewRepository = questionReviewRepository;
    }

    public void execute(QuestionReviewProcessingPayload payload) {
        var question = questionRepository.getQuestionByID(payload.questionId());
        var review = questionReviewRepository.getReviewForQuestionOrCreateNew(payload.userId(), question);

        int quality = getQualityFromStatus(payload.status());

        review.review(quality);
        questionReviewRepository.update(review);
    }

    private int getQualityFromStatus(AttemptStatus status) {
        return switch (status) {
            case Success -> 5;
            case Failure -> 0;
        };
    }
}
