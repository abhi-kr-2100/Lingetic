package com.munetmo.lingetic.LanguageTestService.UseCases;

import com.munetmo.lingetic.LanguageTestService.Entities.Language;
import com.munetmo.lingetic.LanguageTestService.Entities.QuestionList;
import com.munetmo.lingetic.LanguageTestService.Repositories.QuestionListRepository;

import java.util.List;

public class GetQuestionListsForLanguageUseCase {
    private final QuestionListRepository questionListRepository;

    public GetQuestionListsForLanguageUseCase(QuestionListRepository questionListRepository) {
        this.questionListRepository = questionListRepository;
    }

    public List<QuestionList> execute(Language language) {
        return questionListRepository.getQuestionListsByLanguage(language);
    }
}
