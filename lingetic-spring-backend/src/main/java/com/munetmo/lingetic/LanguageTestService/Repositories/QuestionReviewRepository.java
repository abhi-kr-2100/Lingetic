package com.munetmo.lingetic.LanguageTestService.Repositories;

import com.munetmo.lingetic.LanguageTestService.Entities.Language;
import com.munetmo.lingetic.LanguageTestService.Entities.QuestionReview;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.Question;

import java.util.List;

public interface QuestionReviewRepository {
    List<QuestionReview> getTopQuestionsToReview(String userID, Language language, int limit);
    List<QuestionReview> getAllReviews(String userID);
    void addReview(QuestionReview review);
    void update(QuestionReview review);
    void deleteAllReviews();
    QuestionReview getReviewForQuestionOrCreateNew(String userID, Question question);
}
