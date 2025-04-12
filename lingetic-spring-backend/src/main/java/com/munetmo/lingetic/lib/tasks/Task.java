package com.munetmo.lingetic.lib.tasks;

public record Task<T>(String id, T payload) {
    public Task {
        if (id.isBlank()) {
            throw new IllegalArgumentException("Task id cannot be blank");
        }
    }
}
