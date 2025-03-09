package com.munetmo.lingetic.LanguageTestService.Entities.Questions;

import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptRequests.AttemptRequest;
import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptRequests.FillInTheBlanksAttemptRequest;
import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptResponses.AttemptResponse;
import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptResponses.FillInTheBlanksAttemptResponse;
import com.munetmo.lingetic.LanguageTestService.Entities.AttemptStatus;
import com.munetmo.lingetic.LanguageTestService.Entities.Language;
import com.munetmo.lingetic.LanguageTestService.Entities.LanguageModels.LanguageModel;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

public final class FillInTheBlanksQuestion implements Question {
    private final String id;
    private final Language language;
    private final String questionListId;

    private final static QuestionType questionType = QuestionType.FillInTheBlanks;

    public final String questionText;
    public final String hint;
    public final String answer;
    public final int difficulty;

    public FillInTheBlanksQuestion(String id, Language language, String questionText, @Nullable String hint, String answer, int difficulty, String questionListId) {
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

        if (questionListId.isBlank()) {
            throw new IllegalArgumentException("Question list ID cannot be blank");
        }

        this.id = id;
        this.language = language;
        this.questionText = questionText;
        this.hint = Objects.requireNonNullElse(hint, "");
        this.answer = answer;
        this.difficulty = difficulty;
        this.questionListId = questionListId;
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
    public String getQuestionListID() {
        return questionListId;
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
    public int getDifficulty() {
        return difficulty;
    }

    @Override
    public Map<String, Object> getQuestionTypeSpecificData() {
        return Map.of(
            "questionText", questionText,
            "hint", hint,
            "answer", answer
        );
    }

    public static FillInTheBlanksQuestion createFromQuestionTypeSpecificData(String id, Language language, int difficulty, String questionListId, Map<String, Object> data) {
        if (!data.containsKey("questionText") || !data.containsKey("answer")) {
            throw new IllegalArgumentException("Required fields 'questionText' and 'answer' must be present in data");
        }

        String questionText = (String) data.get("questionText");
        String answer = (String) data.get("answer");
        String hint = (String) data.getOrDefault("hint", "");

        return new FillInTheBlanksQuestion(id, language, questionText, hint, answer, difficulty, questionListId);
    }
}
