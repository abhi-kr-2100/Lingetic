package com.munetmo.lingetic.LanguageTestService.DTOs.Question;

import com.munetmo.lingetic.LanguageService.Entities.Language;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.QuestionType;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public final class FillInTheBlanksQuestionDTO implements QuestionDTO {
    public static final QuestionType questionType = QuestionType.FillInTheBlanks;
    private final String id;
    private final Language language;
    public final String text;
    public final String hint;

    public FillInTheBlanksQuestionDTO(String id, Language language, String text, @Nullable String hint) {
        if (id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }

        if (text.isBlank()) {
            throw new IllegalArgumentException("text must not be blank");
        }

        this.id = id;
        this.language = language;
        this.text = text;
        this.hint = Objects.requireNonNullElse(hint, "");
    }

    @Override
    public String getID() {
        return id;
    }

    @Override
    public QuestionType getQuestionType() {
        return questionType;
    }

    public String getText() {
        return text;
    }

    public String getHint() {
        return hint;
    }

    @Override
    public Language getLanguage() {
        return language;
    }
}
