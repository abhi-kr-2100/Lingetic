package com.munetmo.lingetic.LanguageTestService.infra;

import com.munetmo.lingetic.LanguageTestService.Repositories.QuestionRepository;
import com.munetmo.lingetic.LanguageTestService.Repositories.QuestionReviewRepository;
import com.munetmo.lingetic.LanguageTestService.UseCases.AttemptQuestionUseCase;
import com.munetmo.lingetic.LanguageTestService.UseCases.TakeRegularTestUseCase;
import com.munetmo.lingetic.LanguageTestService.infra.Repositories.InMemory.QuestionInMemoryRepository;
import com.munetmo.lingetic.LanguageTestService.infra.Repositories.InMemory.QuestionReviewInMemoryRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Beans {
    @Bean
    public TakeRegularTestUseCase takeRegularTestUseCase(
            QuestionRepository questionRepository, QuestionReviewRepository questionReviewRepository) {
        return new TakeRegularTestUseCase(questionRepository, questionReviewRepository);
    }

    @Bean
    public QuestionRepository questionRepository(QuestionReviewRepository questionReviewRepository) {
        return new QuestionInMemoryRepository(questionReviewRepository);
    }

    @Bean
    public QuestionReviewRepository questionReviewRepository() {
        return new QuestionReviewInMemoryRepository();
    }

    @Bean
    public AttemptQuestionUseCase attemptQuestionUseCase(
            QuestionRepository questionRepository, QuestionReviewRepository questionReviewRepository) {
        return new AttemptQuestionUseCase(questionRepository, questionReviewRepository);
    }
}
