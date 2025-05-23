package com.munetmo.lingetic.LanguageTestService.Entities.Questions;

import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptRequests.AttemptRequest;
import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptRequests.FillInTheBlanksAttemptRequest;
import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptResponses.AttemptResponse;
import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptResponses.FillInTheBlanksAttemptResponse;
import com.munetmo.lingetic.LanguageTestService.Entities.AttemptStatus;
import com.munetmo.lingetic.LanguageService.Entities.Language;
import com.munetmo.lingetic.LanguageService.Entities.LanguageModels.LanguageModel;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

public final class FillInTheBlanksQuestion implements Question {
    private final String id;
    private final Language language;
    private final String sentenceId;

    private final static QuestionType questionType = QuestionType.FillInTheBlanks;

    public final String questionText;
    public final String hint;
    public final String answer;

    public FillInTheBlanksQuestion(String id, Language language, String questionText, @Nullable String hint, String answer, String sentenceId) {
        if (id.isBlank()) {
            throw new IllegalArgumentException("ID cannot be blank");
        }

        if (questionText.isBlank()) {
            throw new IllegalArgumentException("Question text cannot be blank");
        }

        if (!questionText.matches("^[^_]*_+[^_]*$")) {
            throw new IllegalArgumentException("Question text must contain exactly one blank");
        }

        if (answer.isBlank()) {
            throw new IllegalArgumentException("Answer cannot be blank");
        }

        if (sentenceId.isBlank()) {
            throw new IllegalArgumentException("Sentence ID cannot be blank");
        }

        this.id = id;
        this.language = language;
        this.questionText = questionText;
        this.hint = Objects.requireNonNullElse(hint, "");
        this.answer = answer;
        this.sentenceId = sentenceId;
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
    public Language getLanguage() {
        return language;
    }

    @Override
    public String getSentenceID() {
        return sentenceId;
    }

    @Override
    public AttemptResponse assessAttempt(AttemptRequest request) {
        if (!(request instanceof FillInTheBlanksAttemptRequest typedRequest)) {
            throw new IllegalArgumentException("Invalid request type");
        }

        var areEquivalent = LanguageModel.getLanguageModel(language).areEquivalent(
            typedRequest.getUserResponse(),
            answer
        );

        return new FillInTheBlanksAttemptResponse(
            areEquivalent ? AttemptStatus.Success : AttemptStatus.Failure,
            answer
        );
    }

    @Override
    public Map<String, Object> getQuestionTypeSpecificData() {
        return Map.of(
            "questionText", questionText,
            "hint", hint,
            "answer", answer
        );
    }

    public static FillInTheBlanksQuestion createFromQuestionTypeSpecificData(String id, Language language, String sentenceId, Map<String, Object> data) {
        if (!data.containsKey("questionText") || !data.containsKey("answer")) {
            throw new IllegalArgumentException("Required fields 'questionText' and 'answer' must be present in data");
        }

        var questionText = (String) data.get("questionText");
        var answer = (String) data.get("answer");
        var hint = (String) data.getOrDefault("hint", "");

        return new FillInTheBlanksQuestion(id, language, questionText, hint, answer, sentenceId);
    }
}
