package com.munetmo.lingetic.LanguageTestService.DTOs.Question;

import com.munetmo.lingetic.LanguageTestService.Entities.Questions.QuestionType;
import java.util.Objects;

public record FillInTheBlanksQuestionDTO(String id, String text, String hint) implements QuestionDTO {
    public static final QuestionType questionType = QuestionType.FillInTheBlanks;

    public FillInTheBlanksQuestionDTO {
        Objects.requireNonNull(id, "id must not be null");
        if (id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }

        Objects.requireNonNull(text, "text must not be null");
        if (text.isBlank()) {
            throw new IllegalArgumentException("text must not be blank");
        }

        hint = Objects.requireNonNullElse(hint, "");

    }

    public String getID() {
        return id;
    }

    @Override
    public QuestionType getQuestionType() {
        return questionType;
    }
}
