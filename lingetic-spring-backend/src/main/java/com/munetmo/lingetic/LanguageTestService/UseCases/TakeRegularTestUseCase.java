package com.munetmo.lingetic.LanguageTestService.UseCases;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.munetmo.lingetic.LanguageTestService.DTOs.Question.*;
import com.munetmo.lingetic.LanguageService.Entities.Language;
import com.munetmo.lingetic.LanguageTestService.Repositories.QuestionRepository;
import com.munetmo.lingetic.LanguageTestService.Repositories.QuestionReviewRepository;

public class TakeRegularTestUseCase {
    public static final int limit = 10;

    private final QuestionRepository questionRepository;
    private final QuestionReviewRepository questionReviewRepository;

    public TakeRegularTestUseCase(
        QuestionRepository questionRepository,
        QuestionReviewRepository questionReviewRepository
    ) {
        this.questionRepository = questionRepository;
        this.questionReviewRepository = questionReviewRepository;
    }

    public List<QuestionDTO> execute(String userId, Language language, String questionListId) {
        var now = Instant.now();

        // Some of the questions to review get filtered out by the question list filter.
        // This is a bug, but not a severe one. Ideally, getTopQuestionsToReview should
        // be modified to take a question list ID.
        var questionReviews = questionReviewRepository.getTopQuestionsToReview(userId, language, limit);
        var questionsToReviewNow = questionReviews.stream()
            .filter(r -> r.getNextReviewInstant().isBefore(now))
            .map(r -> questionRepository.getQuestionByID(r.questionID))
            .filter(q -> q.getQuestionListID().equals(questionListId))
            .toList();
        var questionList = new ArrayList<>(questionsToReviewNow);

        int remainingCount = limit - questionList.size();
        var unreviewedQuestions = questionRepository
            .getUnreviewedQuestions(userId, language, remainingCount)
            .stream()
            .filter(q -> q.getQuestionListID().equals(questionListId))
            .toList();
        questionList.addAll(
                unreviewedQuestions.subList(0, Math.min(unreviewedQuestions.size(), remainingCount)));

        int stillRemainingCount = limit - questionList.size();
        var questionsToReviewLater = questionReviews.stream()
            .filter(r -> !r.getNextReviewInstant().isBefore(now))
            .map(r -> questionRepository.getQuestionByID(r.questionID))
            .filter(q -> q.getQuestionListID().equals(questionListId))
            .limit(stillRemainingCount)
            .toList();
        questionList.addAll(questionsToReviewLater);

        return questionList.stream()
            .map(QuestionDTO::fromQuestion)
            .toList();
    }
}
