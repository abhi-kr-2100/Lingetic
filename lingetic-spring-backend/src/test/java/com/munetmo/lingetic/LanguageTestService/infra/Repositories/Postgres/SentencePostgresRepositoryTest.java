package com.munetmo.lingetic.LanguageTestService.infra.Repositories.Postgres;

import com.munetmo.lingetic.LanguageService.Entities.Language;
import com.munetmo.lingetic.LanguageTestService.Entities.Sentence;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
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
public class SentencePostgresRepositoryTest {
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
    private SentencePostgresRepository sentenceRepository;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        sentenceRepository.deleteAllSentences();
    }

    @Test
    void shouldAddSentence() {
        // Arrange
        UUID id = UUID.randomUUID();
        Sentence sentence = new Sentence(
            id,
            Language.English,
            "This is a test sentence.",
            Language.Turkish,
            "Bu bir test cümlesidir.",
            10,
            List.of()
        );

        // Act
        sentenceRepository.addSentence(sentence);
        
        // Assert - Check if the sentence exists in the database
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM sentences WHERE id = ?::uuid",
            Integer.class,
            id.toString()
        );
        
        assertEquals(1, count);
    }

    @Test
    void shouldDeleteAllSentences() {
        // Arrange
        Sentence sentence1 = new Sentence(
            UUID.randomUUID(),
            Language.English,
            "First test sentence.",
            Language.Turkish,
            "İlk test cümlesi.",
            10,
            List.of()
        );
        
        Sentence sentence2 = new Sentence(
            UUID.randomUUID(),
            Language.English,
            "Second test sentence.",
            Language.Turkish,
            "İkinci test cümlesi.",
            10,
            List.of()
        );
        
        sentenceRepository.addSentence(sentence1);
        sentenceRepository.addSentence(sentence2);
        
        // Verify sentences were added
        Integer countBefore = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM sentences",
            Integer.class
        );
        assertEquals(2, countBefore);
        
        // Act
        sentenceRepository.deleteAllSentences();
        
        // Assert
        Integer countAfter = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM sentences",
            Integer.class
        );
        assertEquals(0, countAfter);
    }
}