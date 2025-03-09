package com.munetmo.lingetic.LanguageTestService.infra.Repositories.InMemory;

import com.munetmo.lingetic.LanguageTestService.Entities.Language;
import com.munetmo.lingetic.LanguageTestService.Entities.QuestionReview;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.Question;
import com.munetmo.lingetic.LanguageTestService.Repositories.QuestionReviewRepository;

import java.util.*;

public class QuestionReviewInMemoryRepository implements QuestionReviewRepository {
    private final List<QuestionReview> reviews;

    public QuestionReviewInMemoryRepository() {
        this.reviews = new ArrayList<>();
    }

    @Override
    public List<QuestionReview> getTopQuestionsToReview(String userID, Language language, int limit) {
        return reviews.stream()
            .filter(review -> review.language.equals(language) && review.userID.equals(userID))
            .sorted(Comparator.comparing(QuestionReview::getNextReviewInstant))
            .limit(limit)
            .toList();
    }

    @Override
    public List<QuestionReview> getAllReviews(String userID) {
        return reviews.stream().filter(review -> review.userID.equals(userID)).toList();
    }

    @Override
    public void addReview(QuestionReview review) {
        reviews.removeIf(existingReview -> existingReview.questionID.equals(review.questionID));
        reviews.add(review);
    }

    @Override
    public void update(QuestionReview review) {
        addReview(review);
    }

    @Override
    public void deleteAllReviews() {
        reviews.clear();
    }

    @Override
    public QuestionReview getReviewForQuestionOrCreateNew(String userID, Question question) {
        return reviews.stream()
                .filter(review -> review.questionID.equals(question.getID()) &&
                                 review.userID.equals(userID))
                .findFirst()
                .orElseGet(() -> {
                    var newReview = new QuestionReview(
                        UUID.randomUUID().toString(), 
                        question.getID(),
                        userID,
                        question.getLanguage()
                    );
                    addReview(newReview);
                    return newReview;
                });
    }
}
