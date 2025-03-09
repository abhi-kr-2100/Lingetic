package com.munetmo.lingetic.LanguageTestService.infra;

import com.munetmo.lingetic.LanguageTestService.Repositories.QuestionRepository;
import com.munetmo.lingetic.LanguageTestService.Repositories.QuestionReviewRepository;
import com.munetmo.lingetic.LanguageTestService.UseCases.AttemptQuestionUseCase;
import com.munetmo.lingetic.LanguageTestService.UseCases.TakeRegularTestUseCase;
import com.munetmo.lingetic.LanguageTestService.infra.Repositories.Postgres.QuestionPostgresRepository;
import com.munetmo.lingetic.LanguageTestService.infra.Repositories.Postgres.QuestionReviewPostgresRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class Beans {
    @Bean
    public TakeRegularTestUseCase takeRegularTestUseCase(
            QuestionRepository questionRepository, QuestionReviewRepository questionReviewRepository) {
        return new TakeRegularTestUseCase(questionRepository, questionReviewRepository);
    }

    @Bean
    public QuestionRepository questionRepository(JdbcTemplate jdbcTemplate) {
        return new QuestionPostgresRepository(jdbcTemplate);
    }

    @Bean
    public QuestionReviewRepository questionReviewRepository(JdbcTemplate jdbcTemplate) {
        return new QuestionReviewPostgresRepository(jdbcTemplate);
    }

    @Bean
    public AttemptQuestionUseCase attemptQuestionUseCase(
            QuestionRepository questionRepository, QuestionReviewRepository questionReviewRepository) {
        return new AttemptQuestionUseCase(questionRepository, questionReviewRepository);
    }
}
