package com.munetmo.lingetic.LanguageTestService.infra.Repositories.Postgres;

import com.munetmo.lingetic.LanguageTestService.Entities.Language;
import com.munetmo.lingetic.LanguageTestService.Entities.QuestionReview;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.Question;
import com.munetmo.lingetic.LanguageTestService.Repositories.QuestionReviewRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;
import java.util.UUID;

public class QuestionReviewPostgresRepository implements QuestionReviewRepository {
    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<QuestionReview> reviewMapper = (rs, rowNum) -> {
        var review = new QuestionReview(
            rs.getString("id"),
            rs.getString("question_id"),
            rs.getString("user_id"),
            Language.valueOf(rs.getString("language"))
        );
        
        review.setRepetitions(rs.getInt("repetitions"));
        review.setEaseFactor(rs.getDouble("ease_factor"));
        review.setInterval(rs.getInt("interval"));
        review.setNextReviewInstant(rs.getTimestamp("next_review_instant").toInstant());

        return review;
    };

    public QuestionReviewPostgresRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<QuestionReview> getTopQuestionsToReview(String userID, Language language, int limit) {
        var sql = """
            SELECT * FROM question_reviews
            WHERE user_id = ? AND language = ?
            ORDER BY next_review_instant
            LIMIT ?
            """;
        
        return jdbcTemplate.query(sql, reviewMapper, userID, language.name(), limit);
    }

    @Override
    public List<QuestionReview> getAllReviews(String userID) {
        var sql = "SELECT * FROM question_reviews WHERE user_id = ?";
        return jdbcTemplate.query(sql, reviewMapper, userID);
    }

    @Override
    public void addReview(QuestionReview review) {
        var sql = """
            INSERT INTO question_reviews (
                id, question_id, user_id, language, repetitions, ease_factor, interval, next_review_instant
            ) VALUES (
                ?::uuid, ?::uuid, ?, ?, ?, ?, ?, ?
            )
            """;
            
        jdbcTemplate.update(
            sql,
            review.id,
            review.questionID,
            review.userID,
            review.language.name(),
            review.getRepetitions(),
            review.getEaseFactor(),
            review.getInterval(),
            java.sql.Timestamp.from(review.getNextReviewInstant())
        );
    }

    @Override
    public void update(QuestionReview review) {
        var sql = """
            UPDATE question_reviews SET
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
    public void deleteAllReviews() {
        var sql = "DELETE FROM question_reviews";
        jdbcTemplate.update(sql);
    }

    @Override
    public QuestionReview getReviewForQuestionOrCreateNew(String userID, Question question) {
        var sql = """
            WITH inserted AS (
                INSERT INTO question_reviews (
                    id, question_id, user_id, language
                ) VALUES (
                    ?::uuid, ?::uuid, ?, ?
                )
                ON CONFLICT (question_id, user_id) DO NOTHING
                RETURNING *
            )
            SELECT * FROM inserted
            UNION ALL
            SELECT * FROM question_reviews WHERE question_id = ?::uuid AND user_id = ?
            LIMIT 1;
            """;
        
        var newId = UUID.randomUUID().toString();

        return jdbcTemplate.queryForObject(
            sql,
            reviewMapper,
            newId,
            question.getID(),
            userID,
            question.getLanguage().name(),
            question.getID(),
            userID
        );
    }
}