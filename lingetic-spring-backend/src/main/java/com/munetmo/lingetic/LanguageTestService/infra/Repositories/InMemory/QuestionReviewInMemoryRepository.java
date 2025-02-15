package com.munetmo.lingetic.LanguageTestService.infra.Repositories.InMemory;

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
    public List<QuestionReview> getTopQuestionsToReview(String language, int limit) {
        var questionReviews = reviews.stream()
            .filter(review -> review.language.equals(language))
            .sorted(Comparator.comparing(QuestionReview::getNextReviewInstant))
            .limit(limit)
            .toList();

        return questionReviews;
    }

    @Override
    public List<QuestionReview> getAllReviews() {
        // return a copy to prevent modification of the original list
        return new ArrayList<>(reviews);
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
    public QuestionReview getReviewForQuestionOrCreateNew(Question question) {
        var questionReview = reviews.stream()
                .filter(review -> review.questionID.equals(question.getID()))
                .findFirst()
                .orElseGet(() -> {
                    var newReview = new QuestionReview(
                        UUID.randomUUID().toString(), 
                        question.getID(), 
                        question.getLanguage()
                    );
                    addReview(newReview);
                    return newReview;
                });

        return questionReview;
    }
}
