package com.munetmo.lingetic.LanguageTestService.Entities.Questions;

import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptRequests.AttemptRequest;
import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptRequests.FillInTheBlanksAttemptRequest;
import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptResponses.AttemptResponse;
import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptResponses.FillInTheBlanksAttemptResponse;
import com.munetmo.lingetic.LanguageTestService.Entities.AttemptStatus;

public final class FillInTheBlanksQuestion implements Question {
    private final String id;
    private final static QuestionType questionType = QuestionType.FillInTheBlanks;

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
    public QuestionType getQuestionType() {
        return questionType;
    }

    @Override
    public AttemptResponse assessAttempt(AttemptRequest request) {
        if (!(request instanceof FillInTheBlanksAttemptRequest typedRequest)) {
            throw new IllegalArgumentException("Invalid request type");
        }
        
        return new FillInTheBlanksAttemptResponse(
            typedRequest.userResponse().equals(answer) ? AttemptStatus.Success : AttemptStatus.Failure,
            answer
        );
    }
}
