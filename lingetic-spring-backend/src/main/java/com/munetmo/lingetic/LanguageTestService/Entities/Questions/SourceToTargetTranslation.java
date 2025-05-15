package com.munetmo.lingetic.LanguageTestService.Entities.Questions;

import com.munetmo.lingetic.LanguageService.Entities.Language;
import com.munetmo.lingetic.LanguageService.Entities.LanguageModels.LanguageModel;
import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptRequests.AttemptRequest;
import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptResponses.AttemptResponse;
import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptRequests.SourceToTargetTranslationAttemptRequest;
import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptResponses.SourceToTargetTranslationAttemptResponse;
import com.munetmo.lingetic.LanguageTestService.Entities.AttemptStatus;

import java.util.Map;

public final class SourceToTargetTranslation implements Question {
    private final String id;
    private final Language language;
    private final String questionListId;
    private final static QuestionType questionType = QuestionType.SourceToTargetTranslation;

    public final String targetText;  // The text in target language that user needs to type
    public final String translation; // The English translation shown to user
    public final int difficulty;

    public SourceToTargetTranslation(String id, Language language, String targetText, String translation, int difficulty, String questionListId) {
        if (id.isBlank()) {
            throw new IllegalArgumentException("ID cannot be blank");
        }

        if (targetText.isBlank()) {
            throw new IllegalArgumentException("Target text cannot be blank");
        }

        if (translation.isBlank()) {
            throw new IllegalArgumentException("Translation cannot be blank");
        }

        if (questionListId.isBlank()) {
            throw new IllegalArgumentException("Question list ID cannot be blank");
        }

        this.id = id;
        this.language = language;
        this.targetText = targetText;
        this.translation = translation;
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
    public int getDifficulty() {
        return difficulty;
    }

    @Override
    public AttemptResponse assessAttempt(AttemptRequest request) {
        if (!(request instanceof SourceToTargetTranslationAttemptRequest typedRequest)) {
            throw new IllegalArgumentException("Invalid request type");
        }

        var areEquivalent = LanguageModel.getLanguageModel(language).areEquivalent(
            typedRequest.getUserResponse(),
            targetText
        );

        return new SourceToTargetTranslationAttemptResponse(
            areEquivalent ? AttemptStatus.Success : AttemptStatus.Failure,
            targetText
        );
    }

    @Override
    public Map<String, Object> getQuestionTypeSpecificData() {
        return Map.of(
            "targetText", targetText,
            "translation", translation
        );
    }

    public static SourceToTargetTranslation createFromQuestionTypeSpecificData(String id, Language language, int difficulty, String questionListId, Map<String, Object> data) {
        if (!data.containsKey("targetText") || !data.containsKey("translation")) {
            throw new IllegalArgumentException("Required fields 'targetText' and 'translation' must be present in data");
        }

        var targetText = (String) data.get("targetText");
        var translation = (String) data.get("translation");

        return new SourceToTargetTranslation(id, language, targetText, translation, difficulty, questionListId);
    }
}
