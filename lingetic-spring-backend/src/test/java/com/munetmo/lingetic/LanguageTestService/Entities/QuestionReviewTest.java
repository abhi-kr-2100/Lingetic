package com.munetmo.lingetic.LanguageTestService.Entities;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;

class QuestionReviewTest {
    @Test
    void constructorShouldCreateValidInstance() {
        String id = "testId";
        String questionId = "testQuestionId";
        String language = "en";
        
        var question = new QuestionReview(id, questionId, language);
        
        assertEquals(id, question.id);
        assertEquals(questionId, question.questionID);
        assertEquals(language, question.language);
    }

    @Test
    void constructorShouldThrowExceptionForBlankId() {
        assertThrows(IllegalArgumentException.class, 
            () -> new QuestionReview(" ", "questionId", "en"));
    }

    @Test
    void constructorShouldThrowExceptionForBlankQuestionId() {
        assertThrows(IllegalArgumentException.class, 
            () -> new QuestionReview("id", "", "en"));
    }

    @Test
    void constructorShouldThrowExceptionForBlankLanguage() {
        assertThrows(IllegalArgumentException.class, 
            () -> new QuestionReview("id", "questionId", "    "));
    }

    @Test
    void lowQualityShouldSetNextReviewInstantToARecentInstant() {
        var question = new QuestionReview("id2", "qid2", "en");
        Instant before = Instant.now();

        question.review(2);

        Instant nextReview = question.getNextReviewInstant();
        Duration diff = Duration.between(before, nextReview);
        assertTrue(diff.isPositive());
        assertTrue(diff.toSeconds() <= 1);
    }

    @Test
    void highQualityShouldSetNextReviewInstantToADistantInstant() {
        var question = new QuestionReview("id3", "qid3", "en");
        Instant before = Instant.now();

        question.review(5);

        Instant nextReview = question.getNextReviewInstant();
        Duration diff = Duration.between(before, nextReview);
        assertTrue(diff.toDays() >= 1);
    }

    @Test
    void invalidQualityShouldThrowException() {
        var question = new QuestionReview("id4", "qid4", "en");

        assertThrows(IllegalArgumentException.class, () -> question.review(-1));
        assertThrows(IllegalArgumentException.class, () -> question.review(6));
    }
}
