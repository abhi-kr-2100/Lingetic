package com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptResponses;

import com.munetmo.lingetic.LanguageTestService.Entities.AttemptStatus;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.QuestionType;
import com.munetmo.lingetic.LanguageTestService.Entities.WordExplanation;

import java.util.List;

public final class TranslationAttemptResponse implements AttemptResponse {
    private static final QuestionType questionType = QuestionType.Translation;
    private final AttemptStatus attemptStatus;
    private final String correctAnswer;
    private final List<WordExplanation> sourceWordExplanations;

    public TranslationAttemptResponse(AttemptStatus attemptStatus, String correctAnswer, List<WordExplanation> sourceWordExplanations) {
        if (correctAnswer.isBlank()) {
            throw new IllegalArgumentException("correctAnswer cannot be blank.");
        }

        this.attemptStatus = attemptStatus;
        this.correctAnswer = correctAnswer;
        this.sourceWordExplanations = sourceWordExplanations;
    }

    @Override
    public QuestionType getQuestionType() {
        return questionType;
    }

    @Override
    public AttemptStatus getAttemptStatus() {
        return attemptStatus;
    }

    @Override
    public List<WordExplanation> getSourceWordExplanations() {
        return sourceWordExplanations;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }
}
