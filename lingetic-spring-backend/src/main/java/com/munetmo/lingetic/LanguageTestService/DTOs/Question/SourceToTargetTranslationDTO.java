package com.munetmo.lingetic.LanguageTestService.DTOs.Question;

import com.munetmo.lingetic.LanguageService.Entities.Language;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.QuestionType;

public final class SourceToTargetTranslationDTO implements QuestionDTO {
    public static final QuestionType questionType = QuestionType.SourceToTargetTranslation;
    private final String id;
    private final Language language;
    public final String translation;

    public SourceToTargetTranslationDTO(String id, Language language, String translation) {
        if (id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }

        if (translation.isBlank()) {
            throw new IllegalArgumentException("translation must not be blank");
        }

        this.id = id;
        this.language = language;
        this.translation = translation;
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

    public String getTranslation() {
        return translation;
    }
}
