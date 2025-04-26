package com.munetmo.lingetic.LanguageTestService.Repositories;

import com.munetmo.lingetic.LanguageService.Entities.Language;
import com.munetmo.lingetic.LanguageTestService.Entities.QuestionList;
import com.munetmo.lingetic.LanguageTestService.Exceptions.QuestionListWithIDAlreadyExistsException;

import java.util.List;

public interface QuestionListRepository {
    List<QuestionList> getQuestionListsByLanguage(Language language);
    void deleteAllQuestionLists();
    void addQuestionList(QuestionList questionList) throws QuestionListWithIDAlreadyExistsException;
}
