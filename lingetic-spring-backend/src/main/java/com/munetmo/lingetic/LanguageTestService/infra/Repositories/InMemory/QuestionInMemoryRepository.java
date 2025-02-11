package com.munetmo.lingetic.LanguageTestService.infra.Repositories.InMemory;

import com.munetmo.lingetic.LanguageTestService.Entities.Questions.Question;
import com.munetmo.lingetic.LanguageTestService.Repositories.QuestionRepository;
import com.munetmo.lingetic.LanguageTestService.Exceptions.QuestionNotFoundException;
import com.munetmo.lingetic.LanguageTestService.Exceptions.QuestionWithIDAlreadyExistsException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuestionInMemoryRepository implements QuestionRepository {
    private final Map<String, Question> questions;

    public QuestionInMemoryRepository() {
        questions = new HashMap<>();
    }

    @Override
    public Question getQuestionByID(String id) throws QuestionNotFoundException {
        if (!questions.containsKey(id)) {
            throw new QuestionNotFoundException("Question with ID %s not found.".formatted(id));
        }
        return questions.get(id);
    }

    @Override
    public List<Question> getAllQuestions() {
        return new ArrayList<>(questions.values());
    }

    @Override
    public void addQuestion(Question question) throws QuestionWithIDAlreadyExistsException {
        if (questions.containsKey(question.getID())) {
            throw new QuestionWithIDAlreadyExistsException(
                "Question with ID %s already exists.".formatted(question.getID())
            );
        }
        questions.put(question.getID(), question);
    }

    @Override
    public List<Question> getQuestionsByLanguage(String language) {
        return questions.values().stream()
            .filter(q -> q.getLanguage().equals(language))
            .toList();
    }
}
