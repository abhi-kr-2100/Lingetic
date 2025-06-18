package com.munetmo.lingetic.LanguageTestService.DTOs.Question;

import com.munetmo.lingetic.LanguageService.Entities.Language;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.QuestionType;

public final class TranslationQuestionDTO implements QuestionDTO {
    public static final QuestionType questionType = QuestionType.Translation;
    public final Language translateFromLanguage;
    public final Language translateToLanguage;
    public final String toTranslateText;
    private final String sentenceID;

    public TranslationQuestionDTO(Language translateFromLanguage, Language translateToLanguage, String toTranslateText, String sentenceID) {
        if (toTranslateText.isBlank()) {
            throw new IllegalArgumentException("toTranslateText must not be blank");
        }

        if (sentenceID.isBlank()) {
            throw new IllegalArgumentException("sentenceID must not be blank");
        }

        this.translateFromLanguage = translateFromLanguage;
        this.translateToLanguage = translateToLanguage;
        this.toTranslateText = toTranslateText;
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

    public String getToTranslateText() {
        return toTranslateText;
    }

    public Language getTranslateFromLanguage() {
        return translateFromLanguage;
    }

    public Language getTranslateToLanguage() {
        return translateToLanguage;
    }
}
