package com.munetmo.lingetic.LanguageTestService.Repositories;

import com.munetmo.lingetic.LanguageService.Entities.Language;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.Question;
import com.munetmo.lingetic.LanguageTestService.Exceptions.QuestionNotFoundException;
import com.munetmo.lingetic.LanguageTestService.Exceptions.QuestionWithIDAlreadyExistsException;

import java.util.List;

public interface QuestionRepository {
    Question getQuestionByID(String id) throws QuestionNotFoundException;
    List<Question> getAllQuestions();
    void deleteAllQuestions();
    List<Question> getQuestionsByLanguage(Language language);
    void addQuestion(Question question) throws QuestionWithIDAlreadyExistsException;
    List<Question> getQuestionsBySentenceID(String sentenceID);
}
