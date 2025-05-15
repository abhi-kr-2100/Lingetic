package com.munetmo.lingetic.LanguageTestService.Entities.Questions;

import com.munetmo.lingetic.LanguageService.Entities.Language;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Map;

public class QuestionTest {
    @Test
    public void createFromQuestionTypeSpecificDataShouldCreateFillInTheBlanksQuestion() {
        var id = "1";
        var language = Language.English;
        var difficulty = 1;
        var questionListId = "list1";
        Map<String, Object> data = Map.of(
            "questionText", "Fill in the ___",
            "answer", "blank"
        );

        var question = Question.createFromQuestionTypeSpecificData(id, language, difficulty, questionListId, QuestionType.FillInTheBlanks, data);

        assertNotNull(question);
        assertEquals(QuestionType.FillInTheBlanks, question.getQuestionType());
    }

    @Test
    public void createFromQuestionTypeSpecificDataShouldCreateSourceToTargetTranslation() {
        var id = "1";
        var language = Language.English;
        var difficulty = 1;
        var questionListId = "list1";
        Map<String, Object> data = Map.of(
            "targetText", "Jag heter David.",
            "translation", "I'm David."
        );

        var question = Question.createFromQuestionTypeSpecificData(id, language, difficulty, questionListId, QuestionType.SourceToTargetTranslation, data);

        assertNotNull(question);
        assertEquals(QuestionType.SourceToTargetTranslation, question.getQuestionType());
    }
}
