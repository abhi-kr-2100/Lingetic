package com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptResponses;

import com.munetmo.lingetic.LanguageTestService.Entities.AttemptStatus;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.QuestionType;

public final class SourceToTargetTranslationAttemptResponse implements AttemptResponse {
    private final AttemptStatus attemptStatus;
    private final String correctAnswer;

    public SourceToTargetTranslationAttemptResponse(AttemptStatus attemptStatus, String correctAnswer) {
        if (correctAnswer.isBlank()) {
            throw new IllegalArgumentException("correctAnswer cannot be blank.");
        }

        this.attemptStatus = attemptStatus;
        this.correctAnswer = correctAnswer;
    }

    @Override
    public QuestionType getQuestionType() {
        return QuestionType.SourceToTargetTranslation;
    }

    @Override
    public AttemptStatus getAttemptStatus() {
        return attemptStatus;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }
}
