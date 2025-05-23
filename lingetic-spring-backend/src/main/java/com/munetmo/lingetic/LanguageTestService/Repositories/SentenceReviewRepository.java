package com.munetmo.lingetic.LanguageTestService.Repositories;

import com.munetmo.lingetic.LanguageService.Entities.Language;
import com.munetmo.lingetic.LanguageTestService.Entities.SentenceReview;
import com.munetmo.lingetic.LanguageTestService.Entities.Sentence;

import java.util.List;

public interface SentenceReviewRepository {
    List<SentenceReview> getTopSentencesToReview(String userID, Language language, int limit);
    List<SentenceReview> getAllReviews(String userID);
    void update(SentenceReview review);
    SentenceReview getReviewForSentenceOrCreateNew(String userID, Sentence sentence);
}