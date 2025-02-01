package com.munetmo.lingetic.LanguageTestService.DTOs.Question;

import java.util.Objects;

public final class FillInTheBlanksQuestionDTO implements QuestionDTO {
    public static final String type = "FillInTheBlanks";
    public final String id;
    public final String text;
    public final String hint;

    public FillInTheBlanksQuestionDTO(String id, String text, String hint) {
        Objects.requireNonNull(id, "id must not be null");
        if (id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }

        Objects.requireNonNull(text, "text must not be null");
        if (text.isBlank()) {
            throw new IllegalArgumentException("text must not be blank");
        }

        hint = Objects.requireNonNullElse(hint, "");

        this.id = id;
        this.text = text;
        this.hint = hint;
    }

    public String getID() {
        return id;
    }

    public String getType() {
        return type;
    }
}
