package com.munetmo.lingetic.LanguageTestService.DTOs.Question;

import com.munetmo.lingetic.LanguageService.Entities.Language;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.QuestionType;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public final class SourceToTargetTranslationQuestionDTO implements QuestionDTO {
    public static final QuestionType questionType = QuestionType.SourceToTargetTranslation;
    public final Language sourceLanguage;
    public final Language targetLanguage;
    public final String sourceText;
    private final String sentenceID;

    public SourceToTargetTranslationQuestionDTO(Language sourceLanguage, Language targetLanguage, String sourceText, String sentenceID) {
        if (sourceText.isBlank()) {
            throw new IllegalArgumentException("sourceText must not be blank");
        }

        if (sentenceID.isBlank()) {
            throw new IllegalArgumentException("sentenceID must not be blank");
        }

        this.sourceLanguage = sourceLanguage;
        this.targetLanguage = targetLanguage;
        this.sourceText = sourceText;
        this.sentenceID = sentenceID;
    }

    @Override
    public QuestionType getQuestionType() {
        return questionType;
    }

    @Override
    public String getSentenceID() {
        return sentenceID;
    }

    public String getSourceText() {
        return sourceText;
    }

    public Language getSourceLanguage() {
        return sourceLanguage;
    }

    public Language getTargetLanguage() {
        return targetLanguage;
    }
}
