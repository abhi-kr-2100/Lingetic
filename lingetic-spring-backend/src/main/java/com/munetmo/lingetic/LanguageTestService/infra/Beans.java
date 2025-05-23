package com.munetmo.lingetic.LanguageTestService.infra;

import com.munetmo.lingetic.LanguageTestService.Repositories.*;
import com.munetmo.lingetic.LanguageTestService.UseCases.AttemptQuestionUseCase;
import com.munetmo.lingetic.LanguageTestService.UseCases.TakeRegularTestUseCase;
import com.munetmo.lingetic.LanguageTestService.infra.Repositories.Postgres.*;
import com.munetmo.lingetic.lib.tasks.TaskQueue;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class Beans {
    @Bean
    public TakeRegularTestUseCase takeRegularTestUseCase(
            QuestionRepository questionRepository, 
            SentenceReviewRepository sentenceReviewRepository,
            SentenceRepository sentenceRepository) {
        return new TakeRegularTestUseCase(questionRepository, sentenceReviewRepository, sentenceRepository);
    }

    @Bean
    public QuestionRepository questionRepository(JdbcTemplate jdbcTemplate) {
        return new QuestionPostgresRepository(jdbcTemplate);
    }

    @Bean
    public AttemptQuestionUseCase attemptQuestionUseCase(
            QuestionRepository questionRepository, TaskQueue taskQueue, ExecutorService taskSubmitExecutor) {
        return new AttemptQuestionUseCase(questionRepository, taskQueue, taskSubmitExecutor);
    }

    @Bean(destroyMethod = "shutdown")
    public ExecutorService taskSubmitExecutor() {
        return Executors.newCachedThreadPool();
    }

    @Bean
    public SentenceRepository sentenceRepository(JdbcTemplate jdbcTemplate) {
        return new SentencePostgresRepository(jdbcTemplate);
    }

    @Bean
    public SentenceReviewRepository sentenceReviewRepository(JdbcTemplate jdbcTemplate) {
        return new SentenceReviewPostgresRepository(jdbcTemplate);
    }
}
