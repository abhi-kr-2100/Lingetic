package com.munetmo.lingetic.LanguageTestService.Entities.Questions;

import com.munetmo.lingetic.LanguageTestService.DTOs.Question.FillInTheBlanksQuestionDTO;

public final class FillInTheBlanksQuestion implements Question {
    private final String id;
    private final static QuestionType type = QuestionType.FillInTheBlanks;

    public final String questionText;
    public final String hint;
    public final String answer;

    public FillInTheBlanksQuestion(String id, String questionText, String hint, String answer) {
        this.id = id;
        this.questionText = questionText;
        this.hint = hint;
        this.answer = answer;
    }

    @Override
    public String getID() {
        return id;
    }

    @Override
    public QuestionType getType() {
        return type;
    }

    @Override
    public FillInTheBlanksQuestionDTO toDTO() {
        return new FillInTheBlanksQuestionDTO(getID(), questionText, hint);
    }
}
