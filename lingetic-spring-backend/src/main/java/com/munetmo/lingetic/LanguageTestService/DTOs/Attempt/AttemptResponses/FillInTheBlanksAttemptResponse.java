package com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptResponses;

import com.munetmo.lingetic.LanguageTestService.Entities.AttemptStatus;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.QuestionType;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.FillInTheBlanksQuestion.WordExplanation;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public final class FillInTheBlanksAttemptResponse implements AttemptResponse {
    private static final QuestionType questionType = QuestionType.FillInTheBlanks;
    private final AttemptStatus attemptStatus;
    private final String correctAnswer;
    private final List<WordExplanation> explanation;

    public FillInTheBlanksAttemptResponse(AttemptStatus attemptStatus, String correctAnswer) {
        this(attemptStatus, correctAnswer, null);
    }

    public FillInTheBlanksAttemptResponse(AttemptStatus attemptStatus, String correctAnswer, @Nullable List<WordExplanation> explanation) {
        if (correctAnswer.isBlank()) {
            throw new IllegalArgumentException("correctAnswer cannot be blank.");
        }

        this.attemptStatus = attemptStatus;
        this.correctAnswer = correctAnswer;
        this.explanation = Objects.requireNonNullElse(explanation, List.of());
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

    public List<WordExplanation> getExplanation() {
        return explanation;
    }
}
