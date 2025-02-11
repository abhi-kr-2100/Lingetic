package com.munetmo.lingetic.LanguageTestService.infra.Repositories.InMemory;

import com.munetmo.lingetic.LanguageTestService.Entities.Questions.Question;
import com.munetmo.lingetic.LanguageTestService.Entities.QuestionToReview;
import com.munetmo.lingetic.LanguageTestService.Repositories.QuestionRepository;
import com.munetmo.lingetic.LanguageTestService.Repositories.QuestionToReviewRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class QuestionToReviewInMemoryRepository implements QuestionToReviewRepository {
    private final List<QuestionToReview> reviews;
    private final QuestionRepository questionRepository;

    public QuestionToReviewInMemoryRepository(QuestionRepository questionRepository) {
        this.reviews = new ArrayList<>();
        this.questionRepository = questionRepository;
    }

    @Override
    public List<Question> getTopQuestionsToReview(String language, int limit) {
        var questionIds = reviews.stream()
            .filter(review -> review.language.equals(language))
            .filter(review -> review.getNextReviewInstant().isBefore(Instant.now()))
            .sorted(Comparator.comparing(QuestionToReview::getNextReviewInstant))
            .limit(limit)
            .map(review -> review.questionID).toList();

        var questions = questionIds.stream()
                .map(questionRepository::getQuestionByID).toList();

        return questions;
    }

    @Override
    public void addReview(QuestionToReview review) {
        reviews.removeIf(existingReview -> existingReview.questionID.equals(review.questionID));
        reviews.add(review);
    }

    @Override
    public void update(QuestionToReview review) {
        addReview(review);
    }

    @Override
    public QuestionToReview getReviewByQuestionIDOrCreate(String questionID) {
        var questionToReview = reviews.stream()
                .filter(review -> review.questionID.equals(questionID))
                .findFirst()
                .orElseGet(() -> {
                    var question = questionRepository.getQuestionByID(questionID);
                    var newReview = new QuestionToReview(UUID.randomUUID().toString(), questionID, question.getLanguage());
                    addReview(newReview);
                    return newReview;
                });

        return questionToReview;
    }
}
