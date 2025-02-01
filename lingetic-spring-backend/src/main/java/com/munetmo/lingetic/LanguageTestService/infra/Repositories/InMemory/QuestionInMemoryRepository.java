package com.munetmo.lingetic.LanguageTestService.infra.Repositories.InMemory;

import com.munetmo.lingetic.LanguageTestService.Entities.Questions.Question;
import com.munetmo.lingetic.LanguageTestService.Repositories.QuestionRepository;
import com.munetmo.lingetic.LanguageTestService.Entities.Questions.FillInTheBlanksQuestion;

import java.util.List;

public class QuestionInMemoryRepository implements QuestionRepository {
    private static final List<Question> QUESTIONS = List.of(
            new FillInTheBlanksQuestion(
                "1", 
                "The cat ____ lazily on the windowsill.",
                "straighten or extend one's body",
                "stretched"
            ),
            new FillInTheBlanksQuestion(
                "2",
                "She ____ her coffee every morning.",
                "to drink",
                "drinks"
            ),
            new FillInTheBlanksQuestion(
                "3",
                "The children ____ in the park yesterday.",
                "to have fun or recreation",
                "played"
            ),
            new FillInTheBlanksQuestion(
                "4",
                "He ____ the piano beautifully.",
                "to create music with an instrument",
                "plays"
            ),
            new FillInTheBlanksQuestion(
                "5",
                "They ____ dinner at 7 PM.",
                "to consume food",
                "eat"
            ),
            new FillInTheBlanksQuestion(
                "6",
                "The birds ____ south for the winter.",
                "to move through the air using wings",
                "fly"
            ),
            new FillInTheBlanksQuestion(
                "7",
                "The teacher ____ the lesson clearly.",
                "to give information about something",
                "explains"
            ),
            new FillInTheBlanksQuestion(
                "8",
                "We ____ to the beach every summer.",
                "to travel or move to a place",
                "go"
            ),
            new FillInTheBlanksQuestion(
                "9",
                "The sun ____ in the east.",
                "to come above the horizon",
                "rises"
            ),
            new FillInTheBlanksQuestion(
                "10",
                "She ____ beautiful music on her violin.",
                "to create pleasant sounds",
                "makes"
            )
    );

    @Override
    public Question getQuestionByID(String id) throws Exception {
        var foundQuestion = QUESTIONS.stream().filter(q -> q.getID().equals(id)).findFirst().orElse(null);
        if (foundQuestion == null) {
            throw new Exception("Question not found.");
        }

        return foundQuestion;
    }

    @Override
    public List<Question> getAllQuestions() {
        return QUESTIONS;
    }
}
