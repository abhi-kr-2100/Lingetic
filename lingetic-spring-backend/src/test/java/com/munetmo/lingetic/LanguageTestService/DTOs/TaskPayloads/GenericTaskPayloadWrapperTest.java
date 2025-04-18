package com.munetmo.lingetic.LanguageTestService.DTOs.TaskPayloads;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GenericTaskPayloadWrapperTest {
    static class DummyPayload {
        private final String content;

        public DummyPayload(String content) {
            this.content = content;
        }

        public String getContent() {
            return content;
        }
    }

    @Test
    void shouldCreateWrapperWithValidParameters() {
        DummyPayload dummy = new DummyPayload("test data");
        GenericTaskPayloadWrapper<DummyPayload> wrapper = new GenericTaskPayloadWrapper<>("task-001", dummy);

        assertEquals("task-001", wrapper.getId());
        assertEquals("test data", wrapper.getPayload().getContent());
    }

    @Test
    void shouldThrowExceptionWhenIdIsBlank() {
        DummyPayload dummy = new DummyPayload("dummy");
        assertThrows(
                IllegalArgumentException.class,
                () -> new GenericTaskPayloadWrapper<>("", dummy));
    }
}
