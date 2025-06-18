package com.munetmo.lingetic.LanguageTestService.Entities.Questions;

import com.munetmo.lingetic.LanguageService.Entities.Language;
import com.munetmo.lingetic.LanguageTestService.Entities.WordExplanation;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class QuestionTest {
    @Test
    public void createFromQuestionTypeSpecificDataShouldCreateFillInTheBlanksQuestion() {
        var id = "1";
        var language = Language.English;
        var sentenceId = "sentence1";
        
        Map<String, Object> data = Map.of(
            "questionText", "Fill in the ___",
            "answer", "blank"
        );

        var question = Question.createFromQuestionTypeSpecificData(id, language, sentenceId, QuestionType.FillInTheBlanks, Collections.emptyList(), data);

        assertNotNull(question);
        assertEquals(QuestionType.FillInTheBlanks, question.getQuestionType());
        assertEquals(sentenceId, question.getSentenceID());
        assertTrue(question.getSourceWordExplanations().isEmpty());
    }
    
    @Test
    public void createFromQuestionTypeSpecificDataShouldCreateFillInTheBlanksQuestionWithWordExplanations() {
        var id = "1";
        var language = Language.English;
        var sentenceId = "sentence1";
        var wordExplanations = List.of(
            new WordExplanation(0, "Fill", List.of("verb"), "To complete something"),
            new WordExplanation(8, "the", List.of("article"), "Definite article")
        );
        
        Map<String, Object> data = Map.of(
            "questionText", "Fill in the ___",
            "answer", "blank"
        );

        var question = Question.createFromQuestionTypeSpecificData(id, language, sentenceId, QuestionType.FillInTheBlanks, wordExplanations, data);

        assertNotNull(question);
        assertEquals(QuestionType.FillInTheBlanks, question.getQuestionType());
        assertEquals(sentenceId, question.getSentenceID());
        assertEquals(wordExplanations, question.getSourceWordExplanations());
    }

    @Test
    public void createFromQuestionTypeSpecificDataShouldCreateSourceToTargetTranslationQuestion() {
        var id = "1";
        var language = Language.English;
        var sentenceId = "sentence1";

        Map<String, Object> data = Map.of(
            "sourceLanguage", Language.English.name(),
            "targetLanguage", Language.Turkish.name(),
            "sourceText", "Hello",
            "targetText", "Merhaba"
        );

        var question = Question.createFromQuestionTypeSpecificData(id, language, sentenceId, QuestionType.SourceToTargetTranslation, Collections.emptyList(), data);

        assertNotNull(question);
        assertEquals(QuestionType.SourceToTargetTranslation, question.getQuestionType());
        assertEquals(sentenceId, question.getSentenceID());
        assertTrue(question.getSourceWordExplanations().isEmpty());
    }
}
