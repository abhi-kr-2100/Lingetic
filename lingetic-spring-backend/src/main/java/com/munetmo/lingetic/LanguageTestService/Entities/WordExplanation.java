package com.munetmo.lingetic.LanguageTestService.Entities;

import java.util.List;

public record WordExplanation(int startIndex, String word, List<String> properties, String comment) {
    public WordExplanation {
        if (startIndex < 0) {
            throw new IllegalArgumentException("startIndex must be non-negative");
        }
        if (word.isBlank()) {
            throw new IllegalArgumentException("Word cannot be blank");
        }
        if (properties.stream().anyMatch(String::isBlank)) {
            throw new IllegalArgumentException("Properties cannot contain blank strings");
        }
        if (comment.isBlank()) {
            throw new IllegalArgumentException("Comment cannot be blank");
        }
    }
}
