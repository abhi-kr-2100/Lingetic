package com.munetmo.lingetic.LanguageTestService.DTOs.Question;

import com.munetmo.lingetic.LanguageService.Entities.Language;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.QuestionType;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public final class FillInTheBlanksQuestionDTO implements QuestionDTO {
    public static final QuestionType questionType = QuestionType.FillInTheBlanks;
    public final String text;
    public final String hint;
    private final String sentenceID;

    public FillInTheBlanksQuestionDTO(String text, @Nullable String hint, String sentenceID) {
        if (text.isBlank()) {
            throw new IllegalArgumentException("text must not be blank");
        }

        if (sentenceID.isBlank()) {
            throw new IllegalArgumentException("sentenceID must not be blank");
        }

        this.text = text;
        this.hint = Objects.requireNonNullElse(hint, "");
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

    public String getText() {
        return text;
    }

    public String getHint() {
        return hint;
    }
}
