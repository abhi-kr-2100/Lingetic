package com.munetmo.lingetic.LanguageTestService.UseCases;

import java.util.List;

import com.munetmo.lingetic.LanguageTestService.DTOs.Question.*;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.Question;
import com.munetmo.lingetic.LanguageTestService.Repositories.QuestionRepository;

public class TakeRegularTestUseCase {
    private QuestionRepository questionRepository;

    public TakeRegularTestUseCase(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    public List<QuestionDTO> execute() {
        return questionRepository.getAllQuestions().stream().map(QuestionDTO::fromQuestion).toList();
    }
}
