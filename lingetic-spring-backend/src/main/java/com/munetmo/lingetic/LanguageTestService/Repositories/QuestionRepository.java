package com.munetmo.lingetic.LanguageTestService.Repositories;

import com.munetmo.lingetic.LanguageTestService.Entities.Questions.Question;
import com.munetmo.lingetic.LanguageTestService.Exceptions.QuestionNotFoundException;
import com.munetmo.lingetic.LanguageTestService.Exceptions.QuestionWithIDAlreadyExistsException;

import java.util.List;

public interface QuestionRepository {
    Question getQuestionByID(String id) throws QuestionNotFoundException;
    List<Question> getAllQuestions();
    List<Question> getQuestionsByLanguage(String language);
    void addQuestion(Question question) throws QuestionWithIDAlreadyExistsException;
    List<Question> getUnreviewedQuestions(String language, int limit);
}
