package com.munetmo.lingetic.LanguageTestService.infra.Repositories.InMemory;

import com.munetmo.lingetic.LanguageTestService.Entities.Questions.Question;
import com.munetmo.lingetic.LanguageTestService.Repositories.QuestionRepository;
import com.munetmo.lingetic.LanguageTestService.Exceptions.QuestionNotFoundException;
import com.munetmo.lingetic.LanguageTestService.Exceptions.QuestionWithIDAlreadyExistsException;
import com.munetmo.lingetic.LanguageTestService.Repositories.QuestionReviewRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuestionInMemoryRepository implements QuestionRepository {
    private final Map<String, Question> questions;
    private final QuestionReviewRepository questionReviewRepository;

    public QuestionInMemoryRepository(QuestionReviewRepository questionReviewRepository) {
        questions = new HashMap<>();
        this.questionReviewRepository = questionReviewRepository;
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

    @Override
    public List<Question> getUnreviewedQuestions(String language, int limit) {
        var reviewedQuestions = questionReviewRepository.getAllReviews();
        return getQuestionsByLanguage(language).stream()
            .filter(q -> reviewedQuestions.stream().noneMatch(r -> r.questionID.equals(q.getID())))
            .limit(limit)
            .toList();
    }
}
