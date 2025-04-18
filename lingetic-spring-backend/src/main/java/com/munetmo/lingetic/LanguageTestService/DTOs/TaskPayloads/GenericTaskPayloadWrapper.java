package com.munetmo.lingetic.LanguageTestService.DTOs.TaskPayloads;

public class GenericTaskPayloadWrapper<T> {
    private final String id;
    private final T payload;

    public GenericTaskPayloadWrapper(String id, T payload) {
        if (id.isBlank()) {
            throw new IllegalArgumentException("Task ID cannot be blank");
        }
        this.id = id;
        this.payload = payload;
    }

    public String getId() {
        return id;
    }

    public T getPayload() {
        return payload;
    }
}
