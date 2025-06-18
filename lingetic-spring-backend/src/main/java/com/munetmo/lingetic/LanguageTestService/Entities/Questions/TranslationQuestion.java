package com.munetmo.lingetic.LanguageTestService.Entities.Questions;

import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptRequests.AttemptRequest;
import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptRequests.TranslationAttemptRequest;
import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptResponses.AttemptResponse;
import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptResponses.TranslationAttemptResponse;
import com.munetmo.lingetic.LanguageTestService.Entities.AttemptStatus;
import com.munetmo.lingetic.LanguageService.Entities.Language;
import com.munetmo.lingetic.LanguageService.Entities.LanguageModels.LanguageModel;
import com.munetmo.lingetic.LanguageTestService.Entities.WordExplanation;

import java.util.List;
import java.util.Map;

public final class TranslationQuestion implements Question {
    private final String id;
    public final Language translateFromLanguage;
    public final Language translateToLanguage;
    public final String toTranslateText;
    public final String translatedText;
    private final String sentenceId;
    private final List<WordExplanation> sourceWordExplanations;

    private final static QuestionType questionType = QuestionType.Translation;

    public TranslationQuestion(
            String id,
            Language translateFromLanguage,
            Language translateToLanguage,
            String toTranslateText,
            String translatedText,
            String sentenceId,
            List<WordExplanation> sourceWordExplanations) {
        if (id.isBlank()) {
            throw new IllegalArgumentException("ID cannot be blank");
        }

        if (toTranslateText.isBlank()) {
            throw new IllegalArgumentException("Source text cannot be blank");
        }

        if (translatedText.isBlank()) {
            throw new IllegalArgumentException("Target text cannot be blank");
        }

        if (sentenceId.isBlank()) {
            throw new IllegalArgumentException("Sentence ID cannot be blank");
        }

        this.id = id;
        this.translateFromLanguage = translateFromLanguage;
        this.translateToLanguage = translateToLanguage;
        this.toTranslateText = toTranslateText;
        this.translatedText = translatedText;
        this.sentenceId = sentenceId;
        this.sourceWordExplanations = sourceWordExplanations;
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
        return translateToLanguage;
    }

    @Override
    public String getSentenceID() {
        return sentenceId;
    }

    @Override
    public List<WordExplanation> getSourceWordExplanations() {
        return sourceWordExplanations;
    }

    @Override
    public AttemptResponse assessAttempt(AttemptRequest request) {
        if (!(request instanceof TranslationAttemptRequest typedRequest)) {
            throw new IllegalArgumentException("Invalid request type");
        }

        var areEquivalent = LanguageModel.getLanguageModel(translateToLanguage).areEquivalent(
            typedRequest.getUserResponse(),
            translatedText
        );

        return new TranslationAttemptResponse(
            areEquivalent ? AttemptStatus.Success : AttemptStatus.Failure,
            translatedText,
            sourceWordExplanations
        );
    }

    @Override
    public Map<String, Object> getQuestionTypeSpecificData() {
        return Map.of(
            "translateFromLanguage", translateFromLanguage.name(),
            "translateToLanguage", translateToLanguage.name(),
            "toTranslateText", toTranslateText,
            "translatedText", translatedText
        );
    }

    public static TranslationQuestion createFromQuestionTypeSpecificData(String id, Language language, String sentenceId, List<WordExplanation> sourceWordExplanations, Map<String, Object> data) {
        if (!data.containsKey("translateFromLanguage") || !data.containsKey("translateToLanguage") || !data.containsKey("toTranslateText") || !data.containsKey("translatedText")) {
            throw new IllegalArgumentException("Required fields missing in data.");
        }

        var translateFromLanguage = Language.valueOf((String) data.get("translateFromLanguage"));
        var translateToLanguage = Language.valueOf((String) data.get("translateToLanguage"));
        var toTranslateText = (String) data.get("toTranslateText");
        var translatedText = (String) data.get("translatedText");

        return new TranslationQuestion(id, translateFromLanguage, translateToLanguage, toTranslateText, translatedText, sentenceId, sourceWordExplanations);
    }
}
