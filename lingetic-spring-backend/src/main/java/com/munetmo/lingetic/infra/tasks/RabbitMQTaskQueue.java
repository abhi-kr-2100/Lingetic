package com.munetmo.lingetic.infra.tasks;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.munetmo.lingetic.lib.tasks.Task;
import com.munetmo.lingetic.lib.tasks.TaskQueue;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

public class RabbitMQTaskQueue implements TaskQueue {
    private final RabbitTemplate rabbitTemplate;
    private final RabbitAdmin rabbitAdmin;
    private final ObjectMapper objectMapper;

    public RabbitMQTaskQueue(
            RabbitTemplate rabbitTemplate,
            RabbitAdmin rabbitAdmin,
            ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.rabbitAdmin = rabbitAdmin;
        this.objectMapper = objectMapper;
    }

    @Override
    public <T> void submitTask(String taskId, T payload, String queueName) {
        ensureQueueExists(queueName);

        var task = new Task<T>(taskId, payload);
        String message;
        try {
            message = objectMapper.writeValueAsString(task);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize task", e);
        }

        rabbitTemplate.convertAndSend(queueName, message);
    }

    private void ensureQueueExists(String queueName) {
        rabbitAdmin.declareQueue(new Queue(queueName, true));
    }
}
