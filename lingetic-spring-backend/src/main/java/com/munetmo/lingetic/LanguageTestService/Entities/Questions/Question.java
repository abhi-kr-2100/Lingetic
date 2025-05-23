package com.munetmo.lingetic.LanguageTestService.Entities.Questions;

import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptRequests.AttemptRequest;
import com.munetmo.lingetic.LanguageTestService.DTOs.Attempt.AttemptResponses.AttemptResponse;
import com.munetmo.lingetic.LanguageService.Entities.Language;

import java.util.Map;

public sealed interface Question permits FillInTheBlanksQuestion {
    String getID();
    QuestionType getQuestionType();
    Language getLanguage();
    int getDifficulty();
    String getQuestionListID();
    String getSentenceID(); // New method for sentence ID

    Map<String, Object> getQuestionTypeSpecificData();
    static Question createFromQuestionTypeSpecificData(String id, Language language, int difficulty, String questionListId, String sentenceId, QuestionType questionType, Map<String, Object> data) {
        return switch (questionType) {
            case FillInTheBlanks -> FillInTheBlanksQuestion.createFromQuestionTypeSpecificData(id, language, difficulty, questionListId, sentenceId, data);
        };
    }

    AttemptResponse assessAttempt(AttemptRequest request);
}
