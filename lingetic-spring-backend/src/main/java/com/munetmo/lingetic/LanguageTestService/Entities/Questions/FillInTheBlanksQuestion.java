package com.munetmo.lingetic.LanguageTestService.Entities.Questions;

import com.munetmo.lingetic.LanguageTestService.DTOs.Question.FillInTheBlanksQuestionDTO;
import com.munetmo.lingetic.LanguageTestService.Entities.Assessments.FillInTheBlanksAssessment;
import com.munetmo.lingetic.LanguageTestService.Entities.AttemptStatus;
import com.munetmo.lingetic.LanguageTestService.Entities.UserResponses.FillInTheBlanksUserResponse;
import com.munetmo.lingetic.LanguageTestService.Entities.UserResponses.UserResponse;

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

    @Override
    public FillInTheBlanksAssessment assess(UserResponse userResponse) {
        assert(userResponse.getType().equals(getType()));

        var typedUserResponse = (FillInTheBlanksUserResponse)userResponse;
        if (typedUserResponse.getAnswer().equals(answer)) {
            return new FillInTheBlanksAssessment(AttemptStatus.Success, "Good job!", answer);
        }
        return new FillInTheBlanksAssessment(AttemptStatus.Failure, "Try again!", answer);
    }
}
