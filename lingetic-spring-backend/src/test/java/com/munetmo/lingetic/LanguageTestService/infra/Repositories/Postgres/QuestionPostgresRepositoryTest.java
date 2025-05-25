package com.munetmo.lingetic.LanguageTestService.infra.Repositories.Postgres;

import com.munetmo.lingetic.LanguageService.Entities.Language;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.FillInTheBlanksQuestion;
import com.munetmo.lingetic.LanguageTestService.Entities.Sentence;
import com.munetmo.lingetic.LanguageTestService.Exceptions.QuestionNotFoundException;
import com.munetmo.lingetic.LanguageTestService.Exceptions.QuestionWithIDAlreadyExistsException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
public class QuestionPostgresRepositoryTest {
    @Container
    @ServiceConnection
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    private static final String TEST_SENTENCE_ID = UUID.randomUUID().toString();

    @Autowired
    private QuestionPostgresRepository questionRepository;

    @Autowired
    private SentencePostgresRepository sentenceRepository;

    @BeforeEach
    void setUp() {
        questionRepository.deleteAllQuestions();
        sentenceRepository.deleteAllSentences();

        sentenceRepository.addSentence(new Sentence(
            UUID.fromString(TEST_SENTENCE_ID),
            Language.English,
            "The cat stretched lazily on the windowsill.",
            Language.Turkish,
            "Kedi pencere eşiğinde tembelce gerildi.",
            10,
            List.of()
        ));
    }

    @Test
    void shouldAddQuestion() {
        var question = new FillInTheBlanksQuestion(
            UUID.randomUUID().toString(),
            Language.English,
            "The cat ____ lazily on the windowsill.",
            "straighten or extend one's body",
            "stretched",
            TEST_SENTENCE_ID,
            List.of()
        );

        questionRepository.addQuestion(question);

        var result = questionRepository.getQuestionByID(question.getID());

        assertInstanceOf(FillInTheBlanksQuestion.class, result);

        var typedResult = (FillInTheBlanksQuestion) result;

        assertEquals(question.getID(), typedResult.getID());
        assertEquals(question.getLanguage(), typedResult.getLanguage());
        assertEquals(question.getQuestionType(), typedResult.getQuestionType());
        assertEquals(question.questionText, typedResult.questionText);
        assertEquals(question.hint, typedResult.hint);
        assertEquals(question.answer, typedResult.answer);
    }

    @Test
    void shouldThrowExceptionWhenAddingQuestionWithExistingID() {
        var commonId = UUID.randomUUID().toString();
        var sentenceId = TEST_SENTENCE_ID;

        var question1 = new FillInTheBlanksQuestion(
            commonId,
            Language.English,
            "The cat ____ lazily on the windowsill.",
            "straighten or extend one's body",
            "stretched",
            sentenceId,
            List.of()
        );

        var question2 = new FillInTheBlanksQuestion(
            commonId,
            Language.English,
            "The cat ____ lazily on the windowsill.",
            "straighten or extend one's body",
            "stretched",
            sentenceId,
            List.of()
        );

        questionRepository.addQuestion(question1);
        assertThrows(QuestionWithIDAlreadyExistsException.class, () -> questionRepository.addQuestion(question2));
    }

    @Test
    void shouldThrowExceptionWhenQuestionNotFound() {
        var nonExistentId = UUID.randomUUID().toString();
        assertThrows(QuestionNotFoundException.class, () -> questionRepository.getQuestionByID(nonExistentId));
    }

    @Test
    void shouldDeleteAllQuestions() {
        var question = new FillInTheBlanksQuestion(
            UUID.randomUUID().toString(),
            Language.English,
            "Question ___",
            "hint",
            "answer",
            TEST_SENTENCE_ID,
            List.of()
        );

        questionRepository.addQuestion(question);
        assertFalse(questionRepository.getAllQuestions().isEmpty());

        questionRepository.deleteAllQuestions();
        assertTrue(questionRepository.getAllQuestions().isEmpty());
    }

    @Test
    void shouldGetQuestionsBySentenceID() {
        var sentenceId = TEST_SENTENCE_ID;
        var sentenceId2 = UUID.randomUUID().toString();

        sentenceRepository.addSentence(new Sentence(
            UUID.fromString(sentenceId2),
            Language.English,
            "Different sentence.",
            Language.Turkish,
            "Farklı cümle.",
            10,
            List.of()
        ));
        
        var question1 = new FillInTheBlanksQuestion(
            UUID.randomUUID().toString(),
            Language.English,
            "The cat ____ lazily.",
            "hint1",
            "stretched",
            sentenceId,
            List.of()
        );
        
        var question2 = new FillInTheBlanksQuestion(
            UUID.randomUUID().toString(),
            Language.English,
            "The cat stretched ____.",
            "hint2",
            "lazily",
            sentenceId,
            List.of()
        );
        
        var question3 = new FillInTheBlanksQuestion(
            UUID.randomUUID().toString(),
            Language.English,
            "Different sentence ___ question",
            "hint3",
            "answer3",
            sentenceId2,
            List.of()
        );
        
        questionRepository.addQuestion(question1);
        questionRepository.addQuestion(question2);
        questionRepository.addQuestion(question3);
        
        var questions = questionRepository.getQuestionsBySentenceID(sentenceId);
        
        assertEquals(2, questions.size());
        assertTrue(questions.stream().anyMatch(q -> q.getID().equals(question1.getID())));
        assertTrue(questions.stream().anyMatch(q -> q.getID().equals(question2.getID())));
        assertFalse(questions.stream().anyMatch(q -> q.getID().equals(question3.getID())));
        
        // Check that questions are ordered by difficulty
        assertEquals(question1.getID(), questions.get(0).getID());
        assertEquals(question2.getID(), questions.get(1).getID());
    }

    @Test
    void shouldReturnEmptyListWhenNoQuestionsExistForSentenceID() {
        var questions = questionRepository.getQuestionsBySentenceID(UUID.randomUUID().toString());
        assertTrue(questions.isEmpty());
    }
}

