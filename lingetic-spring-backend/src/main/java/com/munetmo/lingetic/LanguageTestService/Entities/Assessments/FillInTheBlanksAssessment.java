package com.munetmo.lingetic.LanguageTestService.Entities.Assessments;

import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptResponse;
import com.munetmo.lingetic.LanguageTestService.Entities.AttemptStatus;

public final class FillInTheBlanksAssessment implements Assessment {
    private final static String type = "FillInTheBlanks";
    private final AttemptStatus status;
    private final String comment;
    public final String answer;

    public FillInTheBlanksAssessment(AttemptStatus status, String comment, String answer) {
        this.status = status;
        this.comment = comment;
        this.answer = answer;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public AttemptStatus getStatus() {
        return status;
    }

    @Override
    public String getComment() {
        return comment;
    }

    @Override
    public AttemptResponse toDTO() {
        return new AttemptResponse(getStatus(), getComment(), answer);
    }
}
