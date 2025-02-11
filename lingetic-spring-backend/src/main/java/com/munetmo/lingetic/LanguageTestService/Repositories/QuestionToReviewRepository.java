package com.munetmo.lingetic.LanguageTestService.Repositories;

import com.munetmo.lingetic.LanguageTestService.Entities.QuestionToReview;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.Question;

import java.util.List;

public interface QuestionToReviewRepository {
    List<Question> getTopQuestionsToReview(String language, int limit);
    void addReview(QuestionToReview review);
    void update(QuestionToReview review);
    QuestionToReview getReviewByQuestionIDOrCreate(String questionID);
}
