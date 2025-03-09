package com.munetmo.lingetic.LanguageTestService.Entities.Questions;

import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptRequests.AttemptRequest;
import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptResponses.AttemptResponse;
import com.munetmo.lingetic.LanguageTestService.Entities.Language;

import java.util.Map;

public sealed interface Question permits FillInTheBlanksQuestion {
    String getID();
    QuestionType getQuestionType();
    Language getLanguage();
    int getDifficulty();
    String getQuestionListID();

    Map<String, Object> getQuestionTypeSpecificData();
    static Question createFromQuestionTypeSpecificData(String id, Language language, int difficulty, String questionListId, QuestionType questionType, Map<String, Object> data) {
        return switch (questionType) {
            case FillInTheBlanks -> FillInTheBlanksQuestion.createFromQuestionTypeSpecificData(id, language, difficulty, questionListId, data);
            default -> throw new IllegalArgumentException("Unsupported question type");
        };
    }

    AttemptResponse assessAttempt(AttemptRequest request);
}
