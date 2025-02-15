package com.munetmo.lingetic.LanguageTestService.Repositories;

import com.munetmo.lingetic.LanguageTestService.Entities.Language;
import com.munetmo.lingetic.LanguageTestService.Entities.QuestionReview;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.Question;

import java.util.List;

public interface QuestionReviewRepository {
    List<QuestionReview> getTopQuestionsToReview(Language language, int limit);
    List<QuestionReview> getAllReviews();
    void addReview(QuestionReview review);
    void update(QuestionReview review);
    QuestionReview getReviewForQuestionOrCreateNew(Question question);
}
