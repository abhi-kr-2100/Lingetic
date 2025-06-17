package com.munetmo.lingetic.LanguageTestService.Entities.Questions;

import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptRequests.AttemptRequest;
import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptRequests.SourceToTargetTranslationAttemptRequest;
import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptResponses.AttemptResponse;
import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptResponses.SourceToTargetTranslationAttemptResponse;
import com.munetmo.lingetic.LanguageTestService.Entities.AttemptStatus;
import com.munetmo.lingetic.LanguageService.Entities.Language;
import com.munetmo.lingetic.LanguageService.Entities.LanguageModels.LanguageModel;
import com.munetmo.lingetic.LanguageTestService.Entities.WordExplanation;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;

public final class SourceToTargetTranslationQuestion implements Question {
    private final String id;
    public final Language sourceLanguage;
    public final Language targetLanguage;
    public final String sourceText;
    public final String targetText;
    private final String sentenceId;
    private final List<WordExplanation> sourceWordExplanations;

    private final static QuestionType questionType = QuestionType.SourceToTargetTranslation;

    public SourceToTargetTranslationQuestion(
            String id,
            Language sourceLanguage,
            Language targetLanguage,
            String sourceText,
            String targetText,
            String sentenceId,
            List<WordExplanation> sourceWordExplanations) {
        if (id.isBlank()) {
            throw new IllegalArgumentException("ID cannot be blank");
        }

        if (sourceText.isBlank()) {
            throw new IllegalArgumentException("Source text cannot be blank");
        }

        if (targetText.isBlank()) {
            throw new IllegalArgumentException("Target text cannot be blank");
        }

        if (sentenceId.isBlank()) {
            throw new IllegalArgumentException("Sentence ID cannot be blank");
        }

        this.id = id;
        this.sourceLanguage = sourceLanguage;
        this.targetLanguage = targetLanguage;
        this.sourceText = sourceText;
        this.targetText = targetText;
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
        return sourceLanguage;
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
        if (!(request instanceof SourceToTargetTranslationAttemptRequest typedRequest)) {
            throw new IllegalArgumentException("Invalid request type");
        }

        var areEquivalent = LanguageModel.getLanguageModel(targetLanguage).areEquivalent(
            typedRequest.getUserResponse(),
            targetText
        );

        return new SourceToTargetTranslationAttemptResponse(
            areEquivalent ? AttemptStatus.Success : AttemptStatus.Failure,
            targetText,
            sourceWordExplanations
        );
    }

    @Override
    public Map<String, Object> getQuestionTypeSpecificData() {
        return Map.of(
            "sourceLanguage", sourceLanguage.name(),
            "targetLanguage", targetLanguage.name(),
            "sourceText", sourceText,
            "targetText", targetText
        );
    }

    public static SourceToTargetTranslationQuestion createFromQuestionTypeSpecificData(String id, Language language, String sentenceId, List<WordExplanation> sourceWordExplanations, Map<String, Object> data) {
        if (!data.containsKey("sourceLanguage") || !data.containsKey("targetLanguage") || !data.containsKey("sourceText") || !data.containsKey("targetText")) {
            throw new IllegalArgumentException("Required fields 'sourceLanguage', 'targetLanguage', 'sourceText', and 'targetText' must be present in data");
        }

        var sourceLanguage = Language.valueOf((String) data.get("sourceLanguage"));
        var targetLanguage = Language.valueOf((String) data.get("targetLanguage"));
        var sourceText = (String) data.get("sourceText");
        var targetText = (String) data.get("targetText");

        return new SourceToTargetTranslationQuestion(id, sourceLanguage, targetLanguage, sourceText, targetText, sentenceId, sourceWordExplanations);
    }
}
