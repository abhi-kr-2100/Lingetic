package com.munetmo.lingetic.LanguageTestService.infra.Repositories.Postgres;

import com.munetmo.lingetic.LanguageTestService.Entities.Language;
import com.munetmo.lingetic.LanguageTestService.Entities.QuestionList;
import com.munetmo.lingetic.LanguageTestService.Exceptions.QuestionListWithIDAlreadyExistsException;
import com.munetmo.lingetic.LanguageTestService.Repositories.QuestionListRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;

public class QuestionListPostgresRepository implements QuestionListRepository {
    private final JdbcTemplate jdbcTemplate;

    public QuestionListPostgresRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<QuestionList> questionListMapper = (rs, rowNum) -> new QuestionList(
            rs.getString("id"),
            rs.getString("name"),
            Language.valueOf(rs.getString("language"))
    );

    @Override
    public List<QuestionList> getQuestionListsByLanguage(Language language) {
        var sql = "SELECT * FROM question_lists WHERE language = ?";
        return jdbcTemplate.query(sql, questionListMapper, language.name());
    }

    @Override
    public void deleteAllQuestionLists() {
        var sql = "DELETE FROM question_lists";
        jdbcTemplate.update(sql);
    }

    @Override
    public void addQuestionList(QuestionList questionList) throws QuestionListWithIDAlreadyExistsException {
        var sql = """
                INSERT INTO question_lists (id, name, language)
                VALUES (?::uuid, ?, ?)
                """;

        try {
            jdbcTemplate.update(sql, questionList.getID(), questionList.getName(), questionList.getLanguage().name());
        } catch (DuplicateKeyException e) {
            throw new QuestionListWithIDAlreadyExistsException("Question list with ID %s already exists.".formatted(questionList.getID()));
        }
    }
}
