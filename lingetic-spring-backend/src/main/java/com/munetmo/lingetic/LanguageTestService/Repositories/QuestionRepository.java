package com.munetmo.lingetic.LanguageTestService.Repositories;

import com.munetmo.lingetic.LanguageTestService.Entities.Questions.Question;

import java.util.List;

public interface QuestionRepository {
    public Question getQuestionByID(String id) throws Exception;
    public List<Question> getAllQuestions();
}
