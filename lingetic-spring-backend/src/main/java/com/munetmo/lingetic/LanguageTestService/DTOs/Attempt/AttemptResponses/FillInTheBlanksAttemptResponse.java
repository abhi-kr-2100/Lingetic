package com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptResponses;

import com.munetmo.lingetic.LanguageTestService.Entities.AttemptStatus;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.QuestionType;

public final class FillInTheBlanksAttemptResponse implements AttemptResponse {
    private static final QuestionType questionType = QuestionType.FillInTheBlanks;
    private final AttemptStatus attemptStatus;
    private final String correctAnswer;

    public FillInTheBlanksAttemptResponse(AttemptStatus attemptStatus, String correctAnswer) {
        if (attemptStatus == null) {
            throw new IllegalArgumentException("attemptStatus cannot be null");
        }

        if (correctAnswer == null || correctAnswer.isBlank()) {
            throw new IllegalArgumentException("correctAnswer cannot be null or blank.");
        }

        this.attemptStatus = attemptStatus;
        this.correctAnswer = correctAnswer;
    }

    @Override
    public QuestionType getQuestionType() {
        return questionType;
    }

    @Override
    public AttemptStatus getAttemptStatus() {
        return attemptStatus;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }
}
