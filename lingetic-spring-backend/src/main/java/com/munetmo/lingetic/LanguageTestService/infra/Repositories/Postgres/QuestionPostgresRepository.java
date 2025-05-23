package com.munetmo.lingetic.LanguageTestService.infra.Repositories.Postgres;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.munetmo.lingetic.LanguageService.Entities.Language;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.Question;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.QuestionType;
import com.munetmo.lingetic.LanguageTestService.Exceptions.QuestionNotFoundException;
import com.munetmo.lingetic.LanguageTestService.Exceptions.QuestionWithIDAlreadyExistsException;
import com.munetmo.lingetic.LanguageTestService.Repositories.QuestionRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;
import java.util.Map;

public class QuestionPostgresRepository implements QuestionRepository {
    private final JdbcTemplate jdbcTemplate;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public QuestionPostgresRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<Question> questionMapper = (rs, rowNum) -> {
        Map<String, Object> questionTypeSpecificData;
        try {
            questionTypeSpecificData = objectMapper.readValue(
                    rs.getString("question_type_specific_data"),
                    new TypeReference<>() {}
            );
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(String.format("Failed to deserialize question type specific data for question %s", rs.getString("id")), e);
        }

        return Question.createFromQuestionTypeSpecificData(
                rs.getString("id"),
                Language.valueOf(rs.getString("language")),
                rs.getString("sentence_id"),
                QuestionType.valueOf(rs.getString("question_type")),
                questionTypeSpecificData
        );
    };

    @Override
    public Question getQuestionByID(String id) throws QuestionNotFoundException {
        var sql = "SELECT * FROM questions WHERE id = ?::uuid";

        Question question;
        try {
            question = jdbcTemplate.queryForObject(sql, questionMapper, id);
        } catch (EmptyResultDataAccessException e) {
            throw new QuestionNotFoundException("Question with ID %s not found.".formatted(id));
        }
        return question;
    }

    @Override
    public List<Question> getAllQuestions() {
        var sql = "SELECT * FROM questions";
        return jdbcTemplate.query(sql, questionMapper);
    }

    @Override
    public void deleteAllQuestions() {
        var sql = "DELETE FROM questions";
        jdbcTemplate.update(sql);
    }

    @Override
    public List<Question> getQuestionsByLanguage(Language language) {
        var sql = "SELECT * FROM questions WHERE language = ?";
        return jdbcTemplate.query(sql, questionMapper, language.name());
    }

    @Override
    public void addQuestion(Question question) throws QuestionWithIDAlreadyExistsException {
        try {
            var sql = """
                INSERT INTO questions (id, question_type, language, question_type_specific_data, sentence_id)
                VALUES (?::uuid, ?, ?, ?::jsonb, ?::uuid)
                """;

            jdbcTemplate.update(
                sql,
                question.getID(),
                question.getQuestionType().name(),
                question.getLanguage().name(),
                objectMapper.writeValueAsString(question.getQuestionTypeSpecificData()),
                question.getSentenceID()
            );
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(String.format("Failed to serialize question %s", question.getID()), e);
        } catch (DuplicateKeyException e) {
            throw new QuestionWithIDAlreadyExistsException("Question with ID %s already exists.".formatted(question.getID()));
        }
    }

    @Override
    public List<Question> getQuestionsBySentenceID(String sentenceID) {
        var sql = """
            SELECT * FROM questions WHERE sentence_id = ?::uuid
            """;
        
        return jdbcTemplate.query(sql, questionMapper, sentenceID);
    }
}
