package com.munetmo.lingetic.LanguageTestService.Entities.Questions;

import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptRequests.AttemptRequest;
import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptRequests.FillInTheBlanksAttemptRequest;
import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptResponses.AttemptResponse;
import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptResponses.FillInTheBlanksAttemptResponse;
import com.munetmo.lingetic.LanguageTestService.Entities.AttemptStatus;

public final class FillInTheBlanksQuestion implements Question {
    private final String id;
    private final String language;
    private final static QuestionType questionType = QuestionType.FillInTheBlanks;

    public final String questionText;
    public final String hint;
    public final String answer;

    public FillInTheBlanksQuestion(String id, String language, String questionText, String hint, String answer) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("ID cannot be null or blank");
        }

        if (language == null || language.isBlank()) {
            throw new IllegalArgumentException("Language cannot be null or blank");
        }

        if (questionText == null || questionText.isBlank()) {
            throw new IllegalArgumentException("Question text cannot be null or blank");
        }

        if (!questionText.matches("^[^_]*_+[^_]*$")) {
            throw new IllegalArgumentException("Question text must contain exactly one blank");
        }

        if (answer == null || answer.isBlank()) {
            throw new IllegalArgumentException("Answer cannot be null or blank");
        }

        this.id = id;
        this.language = language;
        this.questionText = questionText;
        this.hint = hint;
        this.answer = answer;
    }

    @Override
    public String getID() {
        return id;
    }

    @Override
    public QuestionType getQuestionType() {
        return questionType;
    }

    @Override
    public String getLanguage() {
        return language;
    }

    @Override
    public AttemptResponse assessAttempt(AttemptRequest request) {
        if (!(request instanceof FillInTheBlanksAttemptRequest typedRequest)) {
            throw new IllegalArgumentException("Invalid request type");
        }

        return new FillInTheBlanksAttemptResponse(
            typedRequest.getUserResponse().equals(answer) ? AttemptStatus.Success : AttemptStatus.Failure,
            answer
        );
    }
}
