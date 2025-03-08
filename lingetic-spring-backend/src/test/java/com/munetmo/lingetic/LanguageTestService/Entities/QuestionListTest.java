package com.munetmo.lingetic.LanguageTestService.Entities;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class QuestionListTest {
    @Test
    void constructorShouldCreateValidObjectWithCorrectValues() {
        var id = "test-id";
        var name = "Test QuestionList";

        var list = new QuestionList(id, name);

        assertEquals(id, list.getID());
        assertEquals(name, list.getName());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   ", "\t", "\n"})
    void constructorShouldThrowExceptionWhenIdIsInvalid(String id) {
        assertThrows(IllegalArgumentException.class, () ->
            new QuestionList(id, "Test QuestionList")
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   ", "\t", "\n"})
    void constructorShouldThrowExceptionWhenNameIsInvalid(String name) {
        assertThrows(IllegalArgumentException.class, () ->
            new QuestionList("test-id", name)
        );
    }
}