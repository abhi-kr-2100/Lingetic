package com.munetmo.lingetic.LanguageTestService.infra.Repositories.Postgres;

import com.munetmo.lingetic.LanguageService.Entities.Language;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.FillInTheBlanksQuestion;
import com.munetmo.lingetic.LanguageTestService.Entities.QuestionList;
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

    private static final String TEST_QUESTION_LIST_ID = UUID.randomUUID().toString();

    @Autowired
    private QuestionPostgresRepository questionRepository;

    @Autowired
    private QuestionListPostgresRepository questionListRepository;

    @BeforeEach
    void setUp() {
        questionRepository.deleteAllQuestions();
        questionListRepository.deleteAllQuestionLists();

        questionListRepository.addQuestionList(new QuestionList(
            TEST_QUESTION_LIST_ID,
            "Test Question List",
            Language.English
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
            0,
            TEST_QUESTION_LIST_ID
        );

        questionRepository.addQuestion(question);

        var result = questionRepository.getQuestionByID(question.getID());

        assertInstanceOf(FillInTheBlanksQuestion.class, result);

        var typedResult = (FillInTheBlanksQuestion) result;

        assertEquals(question.getID(), typedResult.getID());
        assertEquals(question.getLanguage(), typedResult.getLanguage());
        assertEquals(question.getQuestionListID(), typedResult.getQuestionListID());
        assertEquals(question.getQuestionType(), typedResult.getQuestionType());
        assertEquals(question.questionText, typedResult.questionText);
        assertEquals(question.hint, typedResult.hint);
        assertEquals(question.answer, typedResult.answer);
        assertEquals(question.difficulty, typedResult.difficulty);
    }

    @Test
    void shouldThrowExceptionWhenAddingQuestionWithExistingID() {
        var commonId = UUID.randomUUID().toString();

        var question1 = new FillInTheBlanksQuestion(
            commonId,
            Language.English,
            "The cat ____ lazily on the windowsill.",
            "straighten or extend one's body",
            "stretched",
            0,
            TEST_QUESTION_LIST_ID
        );

        var question2 = new FillInTheBlanksQuestion(
            commonId,
            Language.English,
            "The cat ____ lazily on the windowsill.",
            "straighten or extend one's body",
            "stretched",
            0,
            TEST_QUESTION_LIST_ID
        );

        questionRepository.addQuestion(question1);
        assertThrows(QuestionWithIDAlreadyExistsException.class, () -> questionRepository.addQuestion(question2));
    }

    @Test
    void shouldGetAllQuestionsOrderedByDifficulty() {
        var question1 = new FillInTheBlanksQuestion(
            UUID.randomUUID().toString(),
            Language.English,
            "Question ___",
            "hint",
            "one",
            2,
            TEST_QUESTION_LIST_ID
        );

        var question2 = new FillInTheBlanksQuestion(
            UUID.randomUUID().toString(),
            Language.English,
            "Question ___",
            "hint",
            "two",
            1,
            TEST_QUESTION_LIST_ID
        );

        var question3 = new FillInTheBlanksQuestion(
            UUID.randomUUID().toString(),
            Language.English,
            "Question ___",
            "hint",
            "three",
            3,
            TEST_QUESTION_LIST_ID
        );

        questionRepository.addQuestion(question1);
        questionRepository.addQuestion(question2);
        questionRepository.addQuestion(question3);

        List<FillInTheBlanksQuestion> questions = questionRepository.getAllQuestions()
            .stream()
            .map(q -> (FillInTheBlanksQuestion) q)
            .toList();

        assertEquals(3, questions.size());
        assertEquals(question2.getID(), questions.get(0).getID());
        assertEquals(question1.getID(), questions.get(1).getID());
        assertEquals(question3.getID(), questions.get(2).getID());
    }

    @Test
    void shouldGetQuestionsByLanguage() {
        var englishQuestion = new FillInTheBlanksQuestion(
            UUID.randomUUID().toString(),
            Language.English,
            "The cat ____ lazily.",
            "hint",
            "sleeps",
            0,
            TEST_QUESTION_LIST_ID
        );

        var turkishQuestion = new FillInTheBlanksQuestion(
            UUID.randomUUID().toString(),
            Language.Turkish,
            "Kedi ____ uyuyor.",
            "hint",
            "usulca",
            0,
            TEST_QUESTION_LIST_ID
        );

        questionRepository.addQuestion(englishQuestion);
        questionRepository.addQuestion(turkishQuestion);

        var englishQuestions = questionRepository.getQuestionsByLanguage(Language.English);
        var turkishQuestions = questionRepository.getQuestionsByLanguage(Language.Turkish);

        assertEquals(1, englishQuestions.size());
        assertEquals(1, turkishQuestions.size());
        assertEquals(englishQuestion.getID(), englishQuestions.getFirst().getID());
        assertEquals(turkishQuestion.getID(), turkishQuestions.getFirst().getID());
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
            0,
            TEST_QUESTION_LIST_ID
        );

        questionRepository.addQuestion(question);
        assertFalse(questionRepository.getAllQuestions().isEmpty());

        questionRepository.deleteAllQuestions();
        assertTrue(questionRepository.getAllQuestions().isEmpty());
    }

    @Test
    void shouldGetUnreviewedQuestions() {
        var userId = UUID.randomUUID().toString();

        var question1 = new FillInTheBlanksQuestion(
            UUID.randomUUID().toString(),
            Language.English,
            "Question ___",
            "hint",
            "one",
            1,
            TEST_QUESTION_LIST_ID
        );

        var question2 = new FillInTheBlanksQuestion(
            UUID.randomUUID().toString(),
            Language.English,
            "Question ___",
            "hint",
            "two",
            2,
            TEST_QUESTION_LIST_ID
        );

        questionRepository.addQuestion(question1);
        questionRepository.addQuestion(question2);

        var unreviewedQuestions = questionRepository.getUnreviewedQuestions(userId, Language.English, 10);
        assertEquals(2, unreviewedQuestions.size());
        assertEquals(question1.getID(), unreviewedQuestions.get(0).getID());
        assertEquals(question2.getID(), unreviewedQuestions.get(1).getID());
    }

    @Test
    void shouldRespectLimitInUnreviewedQuestions() {
        var userId = UUID.randomUUID().toString();

        for (int i = 0; i < 5; i++) {
            var question = new FillInTheBlanksQuestion(
                UUID.randomUUID().toString(),
                Language.English,
                "Question ___",
                "hint",
                "answer" + i,
                i,
                TEST_QUESTION_LIST_ID
            );
            questionRepository.addQuestion(question);
        }

        var unreviewedQuestions = questionRepository.getUnreviewedQuestions(userId, Language.English, 3);
        assertEquals(3, unreviewedQuestions.size());
    }
}
