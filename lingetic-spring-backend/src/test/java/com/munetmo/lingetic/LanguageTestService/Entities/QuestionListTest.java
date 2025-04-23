package com.munetmo.lingetic.LanguageTestService.Entities;

import static org.junit.jupiter.api.Assertions.*;

import com.munetmo.lingetic.LanguageService.Entities.Language;
import org.junit.jupiter.api.Test;

class QuestionListTest {

    @Test
    void constructorShouldCreateValidInstance() {
        String id = "testId";
        String name = "Test Question List";
        Language language = Language.English;
        
        var questionList = new QuestionList(id, name, language);
        
        assertEquals(id, questionList.getID());
        assertEquals(name, questionList.getName());
        assertEquals(language, questionList.getLanguage());
    }

    @Test
    void constructorShouldThrowExceptionForBlankId() {
        assertThrows(IllegalArgumentException.class, 
            () -> new QuestionList(" ", "Test Question List", Language.English));
    }

    @Test
    void constructorShouldThrowExceptionForBlankName() {
        assertThrows(IllegalArgumentException.class, 
            () -> new QuestionList("testId", "", Language.English));
    }

    @Test
    void getIDShouldReturnCorrectValue() {
        String id = "testId";
        var questionList = new QuestionList(id, "Test Question List", Language.English);
        
        assertEquals(id, questionList.getID());
    }

    @Test
    void getNameShouldReturnCorrectValue() {
        String name = "Test Question List";
        var questionList = new QuestionList("testId", name, Language.English);
        
        assertEquals(name, questionList.getName());
    }

    @Test
    void getLanguageShouldReturnCorrectValue() {
        Language language = Language.Turkish;
        var questionList = new QuestionList("testId", "Test Question List", language);
        
        assertEquals(language, questionList.getLanguage());
    }
}
