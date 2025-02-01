package com.munetmo.lingetic.LanguageTestService.infra;

import com.munetmo.lingetic.LanguageTestService.Repositories.QuestionRepository;
import com.munetmo.lingetic.LanguageTestService.UseCases.AttemptQuestionUseCase;
import com.munetmo.lingetic.LanguageTestService.UseCases.TakeRegularTestUseCase;
import com.munetmo.lingetic.LanguageTestService.infra.Repositories.InMemory.QuestionInMemoryRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Beans {
    @Bean
    public TakeRegularTestUseCase takeRegularTestUseCase(QuestionRepository questionRepository) {
        return new TakeRegularTestUseCase(questionRepository);
    }

    @Bean
    public QuestionRepository questionRepository() {
        return new QuestionInMemoryRepository();
    }

    @Bean
    public AttemptQuestionUseCase attemptQuestionUseCase() {
        return new AttemptQuestionUseCase();
    }
}
