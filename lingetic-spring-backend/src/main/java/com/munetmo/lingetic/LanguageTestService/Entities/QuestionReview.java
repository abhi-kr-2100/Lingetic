package com.munetmo.lingetic.LanguageTestService.Entities;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class QuestionReview {
    // The SM-2 algorithm doesn't define an upper limit for the value of repetitions. However, since computer memory is
    // finite, a high value has been chosen.
    private final static int MAX_REPETITIONS_VALUE = 1000;

    public final String id;
    public final String questionID;
    public final String language;

    private int repetitions;
    private double easeFactor;
    private int interval;
    private Instant nextReviewInstant;

    public QuestionReview(String id, String questionID, String language) {
        if (id.isBlank()) {
            throw new IllegalArgumentException("id cannot be blank");
        }
        if (questionID.isBlank()) {
            throw new IllegalArgumentException("questionID cannot be blank");
        }
        if (language.isBlank()) {
            throw new IllegalArgumentException("language cannot be blank");
        }

        this.id = id;
        this.questionID = questionID;
        this.language = language;

        this.repetitions = 0;
        this.easeFactor = 2.5;
        this.interval = 0;
        this.nextReviewInstant = Instant.now();
    }

    public void review(int quality) {
        if (quality < 0 || quality > 5) {
            throw new IllegalArgumentException("Quality must be between 0 and 5");
        }

        if (quality < 3) {
            repetitions = 0;
            interval = 0;
        } else {
            repetitions = Math.min(repetitions + 1, MAX_REPETITIONS_VALUE);

            if (repetitions == 1) {
                interval = 1;
            } else if (repetitions == 2) {
                interval = 6;
            } else {
                interval = (int) Math.round(interval * easeFactor);
            }
        }

        easeFactor = Math.max(1.3, 
            easeFactor + 0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02));

        nextReviewInstant = Instant.now().plus(interval, ChronoUnit.DAYS);
    }

    public Instant getNextReviewInstant() {
        return nextReviewInstant;
    }
}
