package com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptResponses;

import com.munetmo.lingetic.LanguageTestService.Entities.AttemptStatus;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.QuestionType;

public final class FillInTheBlanksAttemptResponse implements AttemptResponse {
    private static final QuestionType questionType = QuestionType.FillInTheBlanks;
    private final AttemptStatus attemptStatus;
    public final String comment;
    public final String correctAnswer;

    public FillInTheBlanksAttemptResponse(AttemptStatus attemptStatus, String comment, String correctAnswer) {
        this.attemptStatus = attemptStatus;
        this.comment = comment;
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
}
