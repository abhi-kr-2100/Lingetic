package com.munetmo.lingetic.infra.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
class RabbitMQTaskQueueTest {
    @Container
    @ServiceConnection
    private static final RabbitMQContainer rabbitmq = new RabbitMQContainer("rabbitmq:4-management");

    @DynamicPropertySource
    static void registerRabbitMQProperties(DynamicPropertyRegistry registry) {
        // Disable Flyway migrations since we are not using a database in this test
        registry.add("spring.flyway.enabled", () -> false);

        registry.add("spring.rabbitmq.host", rabbitmq::getHost);
        registry.add("spring.rabbitmq.port", rabbitmq::getAmqpPort);
        registry.add("spring.rabbitmq.username", rabbitmq::getAdminUsername);
        registry.add("spring.rabbitmq.password", rabbitmq::getAdminPassword);
    }

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitAdmin rabbitAdmin;

    @Autowired
    private ObjectMapper objectMapper;

    private RabbitMQTaskQueue taskQueue;

    @BeforeEach
    void setUp() {
        taskQueue = new RabbitMQTaskQueue(rabbitTemplate, rabbitAdmin, objectMapper);
    }

    record TestPayload(String message) {
    }

    @Test
    void shouldSubmitTaskSuccessfully() {
        // Arrange
        var queueName = "test-queue";
        var taskId = "test-task-1";
        var testPayload = new TestPayload("Hello, World!");

        // Act & Assert - should not throw any exceptions
        assertDoesNotThrow(() -> taskQueue.submitTask(taskId, testPayload, queueName));
    }

    @Test
    void shouldSubmitMultipleTasksSuccessfully() {
        // Arrange
        var queueName = "test-queue-2";

        // Act & Assert - should not throw any exceptions
        assertDoesNotThrow(() -> {
            taskQueue.submitTask("task-1", new TestPayload("First"), queueName);
            taskQueue.submitTask("task-2", new TestPayload("Second"), queueName);
        });
    }
}
