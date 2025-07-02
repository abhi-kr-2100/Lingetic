package com.munetmo.lingetic.LanguageTestService.UseCases;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.munetmo.lingetic.LanguageTestService.Entities.Questions.TranslationQuestion;
import com.munetmo.lingetic.LanguageTestService.Entities.Sentence;
import com.munetmo.lingetic.LanguageTestService.Entities.SentenceReview;

import com.munetmo.lingetic.LanguageTestService.DTOs.Question.*;
import com.munetmo.lingetic.LanguageService.Entities.Language;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.Question;
import com.munetmo.lingetic.LanguageTestService.Repositories.QuestionRepository;
import com.munetmo.lingetic.LanguageTestService.Repositories.SentenceReviewRepository;
import com.munetmo.lingetic.LanguageTestService.Repositories.SentenceRepository;
import org.jspecify.annotations.Nullable;

public class TakeRegularTestUseCase {
    public static final int limit = 10;

    private final QuestionRepository questionRepository;
    private final SentenceReviewRepository sentenceReviewRepository;
    private final SentenceRepository sentenceRepository;

    public TakeRegularTestUseCase(
        QuestionRepository questionRepository,
        SentenceReviewRepository sentenceReviewRepository,
        SentenceRepository sentenceRepository
    ) {
        this.questionRepository = questionRepository;
        this.sentenceReviewRepository = sentenceReviewRepository;
        this.sentenceRepository = sentenceRepository;
    }

    public List<QuestionDTO> execute(String userId, Language language) {
        var now = Instant.now();

        var sentenceReviews = sentenceReviewRepository.getTopSentencesToReview(userId, language, limit);
        var sentencesToReviewNow = sentenceReviews.stream()
            .filter(r -> r.getNextReviewInstant().isBefore(now))
            .map(this::getQuestionForSentenceReview)
            .toList();

        var questionList = new ArrayList<>(sentencesToReviewNow);

        int remainingCount = limit - questionList.size();

        var unreviewedSentences = sentenceRepository.getUnreviewedSentences(userId, language, remainingCount);
        var unreviewedQuestions = unreviewedSentences.stream()
            .map(this::getQuestionForSentence)
            .filter(Objects::nonNull)
            .limit(remainingCount)
            .toList();

        questionList.addAll(unreviewedQuestions);

        int stillRemainingCount = limit - questionList.size();
        var sentencesToReviewLater = sentenceReviews.stream()
            .filter(r -> !r.getNextReviewInstant().isBefore(now))
            .map(this::getQuestionForSentenceReview)
            .limit(stillRemainingCount)
            .toList();
        questionList.addAll(sentencesToReviewLater);

        return questionList.stream()
            .map(QuestionDTO::fromQuestion)
            .toList();
    }

    private Question getQuestionForSentenceReview(SentenceReview r) {
        var sentence = sentenceRepository.getSentenceByID(r.sentenceID);
        if (r.getRepetitions() >= 2)
        {
            return new TranslationQuestion(
                UUID.randomUUID().toString(),
                sentence.translationLanguage(),
                sentence.sourceLanguage(),
                sentence.translationText(),
                sentence.sourceText(),
                sentence.id().toString(),
                sentence.sourceWordExplanations()
            );
        }

        var question = getQuestionForSentence(sentence);
        if (question == null) {
            throw new IllegalStateException("No question found for sentence review: " + r.id);
        }
        return question;
    }

    @Nullable
    private Question getQuestionForSentence(Sentence sentence) {
        var questions = questionRepository.getQuestionsBySentenceID(sentence.id().toString());
        if (questions.isEmpty()) {
            return null;
        }
        return questions.getFirst();
    }
}
