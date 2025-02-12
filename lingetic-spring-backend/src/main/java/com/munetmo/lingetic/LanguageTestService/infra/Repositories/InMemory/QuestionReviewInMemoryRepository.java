package com.munetmo.lingetic.LanguageTestService.infra.Repositories.InMemory;

import com.munetmo.lingetic.LanguageTestService.Entities.QuestionReview;
import com.munetmo.lingetic.LanguageTestService.Repositories.QuestionRepository;
import com.munetmo.lingetic.LanguageTestService.Repositories.QuestionReviewRepository;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class QuestionReviewInMemoryRepository implements QuestionReviewRepository {
    private final List<QuestionReview> reviews;
    @Nullable
    private QuestionRepository questionRepository;

    public QuestionReviewInMemoryRepository() {
        this.reviews = new ArrayList<>();
    }

    public void setQuestionRepository(QuestionRepository questionRepository) {
        if (questionRepository == null) {
            throw new IllegalArgumentException("questionRepository cannot be null");
        }

        this.questionRepository = questionRepository;
    }

    @Override
    public List<QuestionReview> getTopQuestionsToReview(String language, int limit) {
        if (questionRepository == null) {
            throw new IllegalStateException("questionRepository must be set before using this repository");
        }

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
    public QuestionReview getReviewByQuestionIDOrCreate(String questionID) {
        if (questionRepository == null) {
            throw new IllegalStateException("questionRepository must be set before using this repository");
        }

        var questionReview = reviews.stream()
                .filter(review -> review.questionID.equals(questionID))
                .findFirst()
                .orElseGet(() -> {
                    if (questionRepository != null) { // this test is required by NullAway
                        var question = questionRepository.getQuestionByID(questionID);
                        var newReview = new QuestionReview(UUID.randomUUID().toString(), questionID, question.getLanguage());
                        addReview(newReview);
                        return newReview;
                    }

                    throw new IllegalStateException("questionRepository must be set before using this repository");
                });

        return questionReview;
    }
}
