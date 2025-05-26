package com.munetmo.lingetic.LanguageTestService.infra.Repositories.Postgres;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.munetmo.lingetic.LanguageService.Entities.Language;
import com.munetmo.lingetic.LanguageTestService.Entities.Sentence;
import com.munetmo.lingetic.LanguageTestService.Entities.WordExplanation;
import com.munetmo.lingetic.LanguageTestService.Repositories.SentenceRepository;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.UUID;

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
            INSERT INTO sentences (id, source_language, source_text, translation_language, translation_text, difficulty, source_word_explanations)
            VALUES (?::uuid, ?, ?, ?, ?, ?, ?::jsonb)
            """;

        String sourceWordExplanationsJson;
        try {
            sourceWordExplanationsJson = objectMapper.writeValueAsString(sentence.sourceWordExplanations());
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize word explanations");
        }

        jdbcTemplate.update(
            sql,
            sentence.id().toString(),
            sentence.sourceLanguage().name(),
            sentence.sourceText(),
            sentence.translationLanguage().name(),
            sentence.translationText(),
            sentence.difficulty(),
            sourceWordExplanationsJson
        );
    }

    @Override
    public Sentence getSentenceByID(String id) {
        var sql = """
            SELECT * FROM sentences WHERE id = ?::uuid
            """;

        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
            var sourceWordExplanationsJson = rs.getString("source_word_explanations");
            List<WordExplanation> sourceWordExplanations;

            try {
                sourceWordExplanations = objectMapper.readValue(
                    sourceWordExplanationsJson,
                    new TypeReference<List<WordExplanation>>() {}
                );
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("Failed to deserialize word explanations");
            }

            return new Sentence(
                UUID.fromString(rs.getString("id")),
                Language.valueOf(rs.getString("source_language")),
                rs.getString("source_text"),
                Language.valueOf(rs.getString("translation_language")),
                rs.getString("translation_text"),
                rs.getInt("difficulty"),
                sourceWordExplanations
            );
        }, id);
    }

    @Override
    public List<Sentence> getUnreviewedSentences(String userID, Language language, int limit) {
        var sql = """
            SELECT s.* FROM sentences s
            WHERE s.source_language = ?
            AND NOT EXISTS (
                SELECT 1 FROM sentence_reviews sr
                WHERE sr.sentence_id = s.id
                AND sr.user_id = ?
            )
            ORDER BY s.difficulty
            LIMIT ?
            """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            var sourceWordExplanationsJson = rs.getString("source_word_explanations");
            List<WordExplanation> sourceWordExplanations;

            try {
                sourceWordExplanations = objectMapper.readValue(
                    sourceWordExplanationsJson,
                    new TypeReference<List<WordExplanation>>() {}
                );
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("Failed to deserialize word explanations");
            }

            return new Sentence(
                UUID.fromString(rs.getString("id")),
                Language.valueOf(rs.getString("source_language")),
                rs.getString("source_text"),
                Language.valueOf(rs.getString("translation_language")),
                rs.getString("translation_text"),
                rs.getInt("difficulty"),
                sourceWordExplanations
            );
        }, language.name(), userID, limit);
    }
}
