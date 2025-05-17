package com.munetmo.lingetic.LanguageTestService.infra;

import com.munetmo.lingetic.LanguageTestService.Repositories.QuestionListRepository;
import com.munetmo.lingetic.LanguageTestService.Repositories.QuestionRepository;
import com.munetmo.lingetic.LanguageTestService.Repositories.QuestionReviewRepository;
import com.munetmo.lingetic.LanguageTestService.UseCases.AttemptQuestionUseCase;
import com.munetmo.lingetic.LanguageTestService.UseCases.GetQuestionListsForLanguageUseCase;
import com.munetmo.lingetic.LanguageTestService.UseCases.TakeRegularTestUseCase;
import com.munetmo.lingetic.LanguageTestService.infra.Repositories.Postgres.QuestionListPostgresRepository;
import com.munetmo.lingetic.LanguageTestService.infra.Repositories.Postgres.QuestionPostgresRepository;
import com.munetmo.lingetic.LanguageTestService.infra.Repositories.Postgres.QuestionReviewPostgresRepository;
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
            QuestionRepository questionRepository, TaskQueue taskQueue, ExecutorService taskSubmitExecutor) {
        return new AttemptQuestionUseCase(questionRepository, taskQueue, taskSubmitExecutor);
    }

    @Bean
    public QuestionListRepository questionListRepository(JdbcTemplate jdbcTemplate) {
        return new QuestionListPostgresRepository(jdbcTemplate);
    }

    @Bean
    public GetQuestionListsForLanguageUseCase getQuestionListsForLanguageUseCase(
            QuestionListRepository questionListRepository) {
        return new GetQuestionListsForLanguageUseCase(questionListRepository);
    }

    @Bean(destroyMethod = "shutdown")
    public ExecutorService taskSubmitExecutor() {
        return Executors.newCachedThreadPool();
    }
}
