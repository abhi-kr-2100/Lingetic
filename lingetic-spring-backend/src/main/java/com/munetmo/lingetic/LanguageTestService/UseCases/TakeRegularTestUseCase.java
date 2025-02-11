package com.munetmo.lingetic.LanguageTestService.UseCases;

import java.util.List;

import com.munetmo.lingetic.LanguageTestService.DTOs.Question.*;
import com.munetmo.lingetic.LanguageTestService.Repositories.QuestionRepository;

public class TakeRegularTestUseCase {
    public static final int limit = 10;

    private final QuestionRepository questionRepository;

    public TakeRegularTestUseCase(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    public List<QuestionDTO> execute(String language) {
        return questionRepository.getQuestionsByLanguage(language).stream()
            .limit(limit)
            .map(QuestionDTO::fromQuestion)
            .toList();
    }

    public List<QuestionDTO> execute() {
        return questionRepository.getAllQuestions().stream()
            .limit(limit)
            .map(QuestionDTO::fromQuestion)
            .toList();
    }
}
