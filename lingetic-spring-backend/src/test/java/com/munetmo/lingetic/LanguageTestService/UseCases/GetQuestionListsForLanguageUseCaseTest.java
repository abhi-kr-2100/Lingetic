package com.munetmo.lingetic.LanguageTestService.UseCases;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.UUID;

import com.munetmo.lingetic.LanguageService.Entities.Language;
import com.munetmo.lingetic.LanguageTestService.Entities.QuestionList;
import com.munetmo.lingetic.LanguageTestService.infra.Repositories.Postgres.QuestionListPostgresRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
class GetQuestionListsForLanguageUseCaseTest {
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
    private GetQuestionListsForLanguageUseCase useCase;

    @Autowired
    private QuestionListPostgresRepository questionListRepository;

    @BeforeEach
    void setUp() {
        questionListRepository.deleteAllQuestionLists();
    }

    @Test
    void shouldReturnEmptyListWhenNoQuestionListsExist() {
        // Act
        List<QuestionList> result = useCase.execute(Language.English);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnQuestionListsForSpecificLanguage() {
        // Arrange
        var englishList1 = new QuestionList(UUID.randomUUID().toString(), "English List 1", Language.English);
        var englishList2 = new QuestionList(UUID.randomUUID().toString(), "English List 2", Language.English);
        var turkishList = new QuestionList(UUID.randomUUID().toString(), "Turkish List", Language.Turkish);

        questionListRepository.addQuestionList(englishList1);
        questionListRepository.addQuestionList(englishList2);
        questionListRepository.addQuestionList(turkishList);

        // Act
        List<QuestionList> englishResults = useCase.execute(Language.English);
        List<QuestionList> turkishResults = useCase.execute(Language.Turkish);

        // Assert
        assertEquals(2, englishResults.size());
        assertEquals(1, turkishResults.size());

        assertTrue(englishResults.stream().anyMatch(list -> list.getID().equals(englishList1.getID())));
        assertTrue(englishResults.stream().anyMatch(list -> list.getID().equals(englishList2.getID())));
        assertTrue(turkishResults.stream().anyMatch(list -> list.getID().equals(turkishList.getID())));
    }

    @Test
    void shouldReturnCorrectQuestionListProperties() {
        // Arrange
        String id = UUID.randomUUID().toString();
        String name = "Test Question List";
        Language language = Language.English;
        
        var questionList = new QuestionList(id, name, language);
        questionListRepository.addQuestionList(questionList);

        // Act
        List<QuestionList> result = useCase.execute(language);

        // Assert
        assertEquals(1, result.size());
        var retrievedList = result.getFirst();
        assertEquals(id, retrievedList.getID());
        assertEquals(name, retrievedList.getName());
        assertEquals(language, retrievedList.getLanguage());
    }
}
