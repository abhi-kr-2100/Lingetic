package com.munetmo.lingetic.LanguageTestService.DTOs.Question;

import com.munetmo.lingetic.LanguageTestService.Entities.Questions.QuestionType;
import java.util.Objects;

public final class FillInTheBlanksQuestionDTO implements QuestionDTO {
    public static final QuestionType questionType = QuestionType.FillInTheBlanks;
    private final String id;
    public final String text;
    public final String hint;

    public FillInTheBlanksQuestionDTO(String id, String text, String hint) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank or null");
        }

        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("text must not be blank");
        }

        this.id = id;
        this.text = text;
        this.hint = Objects.requireNonNullElse(hint, "");
    }

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
}
