package com.munetmo.lingetic.LanguageTestService.Repositories;

import com.munetmo.lingetic.LanguageTestService.Entities.QuestionReview;

import java.util.List;

public interface QuestionReviewRepository {
    List<QuestionReview> getTopQuestionsToReview(String language, int limit);
    List<QuestionReview> getAllReviews();
    void addReview(QuestionReview review);
    void update(QuestionReview review);
    QuestionReview getReviewByQuestionIDOrCreate(String questionID);
}
