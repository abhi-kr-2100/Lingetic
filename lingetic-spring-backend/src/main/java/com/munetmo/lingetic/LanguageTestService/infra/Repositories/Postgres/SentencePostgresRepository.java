package com.munetmo.lingetic.LanguageTestService.infra.Repositories.Postgres;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.munetmo.lingetic.LanguageTestService.Entities.Sentence;
import com.munetmo.lingetic.LanguageTestService.Repositories.SentenceRepository;
import org.springframework.jdbc.core.JdbcTemplate;

public class SentencePostgresRepository implements SentenceRepository {
    private final JdbcTemplate jdbcTemplate;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public SentencePostgresRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void deleteAllSentences() {
        var sql = "DELETE FROM sentences";
        jdbcTemplate.update(sql);
    }

    @Override
    public void addSentence(Sentence sentence) {
        var sql = """
            INSERT INTO sentences (id, source_language, source_text, translation_language, translation_text, source_word_explanations)
            VALUES (?::uuid, ?, ?, ?, ?, ?::jsonb)
            """;

        try {
            var sourceWordExplanationsJson = objectMapper.writeValueAsString(sentence.sourceWordExplanation());
                
            jdbcTemplate.update(
                sql,
                sentence.id().toString(),
                sentence.sourceLanguage().name(),
                sentence.sourceText(),
                sentence.translationLanguage().name(),
                sentence.translationText(),
                sourceWordExplanationsJson
            );
        } catch (Exception e) {
            throw new IllegalStateException("Failed to add sentence: " + e.getMessage(), e);
        }
    }
}