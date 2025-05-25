package com.munetmo.lingetic.LanguageTestService.Repositories;

import com.munetmo.lingetic.LanguageTestService.Exceptions.QuestionNotFoundException;
import com.munetmo.lingetic.LanguageTestService.Exceptions.QuestionWithIDAlreadyExistsException;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.Question;

import java.util.List;

public interface QuestionRepository {
    void addQuestion(Question question) throws QuestionWithIDAlreadyExistsException;
    Question getQuestionByID(String id) throws QuestionNotFoundException;
    Question getQuestionBySentenceID(String sentenceID) throws QuestionNotFoundException;
    List<Question> getQuestionsBySentenceID(String sentenceID);
    List<Question> getAllQuestions();
    void deleteAllQuestions();
}
