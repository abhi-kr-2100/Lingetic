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
        var sentenceId = "sentence1";
        
        Map<String, Object> data = Map.of(
            "questionText", "Fill in the ___",
            "answer", "blank",
            "sentenceId", sentenceId
        );

        var question = Question.createFromQuestionTypeSpecificData(id, language, difficulty, questionListId, sentenceId, QuestionType.FillInTheBlanks, data);

        assertNotNull(question);
        assertEquals(QuestionType.FillInTheBlanks, question.getQuestionType());
        assertEquals(sentenceId, question.getSentenceID());
    }
}
