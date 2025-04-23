package com.munetmo.lingetic.LanguageTestService.infra.Repositories.Postgres;

import com.munetmo.lingetic.LanguageService.Entities.Language;
import com.munetmo.lingetic.LanguageTestService.Entities.QuestionList;
import com.munetmo.lingetic.LanguageTestService.Exceptions.QuestionListWithIDAlreadyExistsException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
public class QuestionListPostgresRepositoryTest {
    @Container
    @ServiceConnection
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private QuestionListPostgresRepository questionListRepository;

    @BeforeEach
    void setUp() {
        questionListRepository.deleteAllQuestionLists();
    }

    @Test
    void shouldAddQuestionList() {
        // Arrange
        String id = UUID.randomUUID().toString();
        String name = "Test Question List";
        Language language = Language.English;
        var questionList = new QuestionList(id, name, language);

        // Act
        questionListRepository.addQuestionList(questionList);
        var retrievedLists = questionListRepository.getQuestionListsByLanguage(language);

        // Assert
        assertEquals(1, retrievedLists.size());
        var retrievedList = retrievedLists.getFirst();
        assertEquals(id, retrievedList.getID());
        assertEquals(name, retrievedList.getName());
        assertEquals(language, retrievedList.getLanguage());
    }

    @Test
    void shouldThrowExceptionWhenAddingDuplicateQuestionList() {
        // Arrange
        String id = UUID.randomUUID().toString();
        String name = "Test Question List";
        Language language = Language.English;
        var questionList = new QuestionList(id, name, language);

        // Act & Assert
        questionListRepository.addQuestionList(questionList);
        assertThrows(QuestionListWithIDAlreadyExistsException.class, () -> {
            questionListRepository.addQuestionList(questionList);
        });
    }

    @Test
    void shouldGetQuestionListsByLanguage() {
        // Arrange
        var englishList1 = new QuestionList(UUID.randomUUID().toString(), "English List 1", Language.English);
        var englishList2 = new QuestionList(UUID.randomUUID().toString(), "English List 2", Language.English);
        var turkishList = new QuestionList(UUID.randomUUID().toString(), "Turkish List", Language.Turkish);

        questionListRepository.addQuestionList(englishList1);
        questionListRepository.addQuestionList(englishList2);
        questionListRepository.addQuestionList(turkishList);

        // Act
        var englishLists = questionListRepository.getQuestionListsByLanguage(Language.English);
        var turkishLists = questionListRepository.getQuestionListsByLanguage(Language.Turkish);

        // Assert
        assertEquals(2, englishLists.size());
        assertEquals(1, turkishLists.size());
        
        // Verify English lists
        assertTrue(englishLists.stream().anyMatch(list -> list.getID().equals(englishList1.getID())));
        assertTrue(englishLists.stream().anyMatch(list -> list.getID().equals(englishList2.getID())));
        
        // Verify Turkish list
        assertEquals(turkishList.getID(), turkishLists.getFirst().getID());
    }

    @Test
    void shouldReturnEmptyListWhenNoQuestionListsExistForLanguage() {
        // Arrange
        var turkishList = new QuestionList(UUID.randomUUID().toString(), "Turkish List", Language.Turkish);
        questionListRepository.addQuestionList(turkishList);

        // Act
        var englishLists = questionListRepository.getQuestionListsByLanguage(Language.English);

        // Assert
        assertTrue(englishLists.isEmpty());
    }

    @Test
    void shouldDeleteAllQuestionLists() {
        // Arrange
        var englishList = new QuestionList(UUID.randomUUID().toString(), "English List", Language.English);
        var turkishList = new QuestionList(UUID.randomUUID().toString(), "Turkish List", Language.Turkish);

        questionListRepository.addQuestionList(englishList);
        questionListRepository.addQuestionList(turkishList);

        // Verify lists were added
        assertFalse(questionListRepository.getQuestionListsByLanguage(Language.English).isEmpty());
        assertFalse(questionListRepository.getQuestionListsByLanguage(Language.Turkish).isEmpty());

        // Act
        questionListRepository.deleteAllQuestionLists();

        // Assert
        assertTrue(questionListRepository.getQuestionListsByLanguage(Language.English).isEmpty());
        assertTrue(questionListRepository.getQuestionListsByLanguage(Language.Turkish).isEmpty());
    }
}
