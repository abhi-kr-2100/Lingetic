package com.munetmo.lingetic.LanguageTestService.infra.Repositories.Postgres;

import com.munetmo.lingetic.LanguageService.Entities.Language;
import com.munetmo.lingetic.LanguageTestService.Entities.SentenceReview;
import com.munetmo.lingetic.LanguageTestService.Entities.Sentence;
import com.munetmo.lingetic.LanguageTestService.Repositories.SentenceReviewRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;
import java.util.UUID;

public class SentenceReviewPostgresRepository implements SentenceReviewRepository {
    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<SentenceReview> reviewMapper = (rs, rowNum) -> {
        var review = new SentenceReview(
            rs.getString("id"),
            rs.getString("sentence_id"),
            rs.getString("user_id"),
            Language.valueOf(rs.getString("language"))
        );
        
        review.setRepetitions(rs.getInt("repetitions"));
        review.setEaseFactor(rs.getDouble("ease_factor"));
        review.setInterval(rs.getInt("interval"));
        review.setNextReviewInstant(rs.getTimestamp("next_review_instant").toInstant());

        return review;
    };

    public SentenceReviewPostgresRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<SentenceReview> getTopSentencesToReview(String userID, Language language, int limit) {
        var sql = """
            SELECT * FROM sentence_reviews
            WHERE user_id = ? AND language = ?
            ORDER BY next_review_instant ASC
            LIMIT ?
            """;
            
        return jdbcTemplate.query(
            sql,
            reviewMapper,
            userID,
            language.name(),
            limit
        );
    }

    @Override
    public List<SentenceReview> getAllReviews(String userID) {
        var sql = """
            SELECT * FROM sentence_reviews
            WHERE user_id = ?
            """;
            
        return jdbcTemplate.query(
            sql,
            reviewMapper,
            userID
        );
    }

    @Override
    public void update(SentenceReview review) {
        var sql = """
            UPDATE sentence_reviews SET
                repetitions = ?,
                ease_factor = ?,
                interval = ?,
                next_review_instant = ?
            WHERE id = ?::uuid
            """;
            
        jdbcTemplate.update(
            sql,
            review.getRepetitions(),
            review.getEaseFactor(),
            review.getInterval(),
            java.sql.Timestamp.from(review.getNextReviewInstant()),
            review.id
        );
    }

    @Override
    public SentenceReview getReviewForSentenceOrCreateNew(String userID, Sentence sentence) {
        var sql = """
            WITH inserted AS (
                INSERT INTO sentence_reviews (
                    id, sentence_id, user_id, language
                ) VALUES (
                    ?::uuid, ?::uuid, ?, ?
                )
                ON CONFLICT (sentence_id, user_id) DO NOTHING
                RETURNING *
            )
            SELECT * FROM inserted
            UNION ALL
            SELECT * FROM sentence_reviews WHERE sentence_id = ?::uuid AND user_id = ?
            LIMIT 1;
            """;
        
        var newId = UUID.randomUUID().toString();

        return jdbcTemplate.queryForObject(
            sql,
            reviewMapper,
            newId,
            sentence.id().toString(),
            userID,
            sentence.sourceLanguage().name(),
            sentence.id().toString(),
            userID
        );
    }
}