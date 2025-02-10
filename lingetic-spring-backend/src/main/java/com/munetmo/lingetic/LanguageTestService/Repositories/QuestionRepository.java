package com.munetmo.lingetic.LanguageTestService.Repositories;

import com.munetmo.lingetic.LanguageTestService.Entities.Questions.Question;
import com.munetmo.lingetic.LanguageTestService.Exceptions.QuestionNotFoundException;

import java.util.List;

public interface QuestionRepository {
    Question getQuestionByID(String id) throws QuestionNotFoundException;
    List<Question> getAllQuestions();
}
