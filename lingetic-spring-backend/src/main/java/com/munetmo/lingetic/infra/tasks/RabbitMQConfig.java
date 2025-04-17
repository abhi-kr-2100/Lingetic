package com.munetmo.lingetic.infra.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.munetmo.lingetic.lib.Utilities;
import com.munetmo.lingetic.lib.tasks.TaskQueue;

import org.jspecify.annotations.Nullable;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    @Value("${spring.rabbitmq.uri}")
    @Nullable
    private String uri;

    @Bean
    public ConnectionFactory connectionFactory() {
        Utilities.assert_(uri != null && !uri.isBlank(), "RabbitMQ URI cannot be null or empty");

        var factory = new CachingConnectionFactory();
        factory.setUri(uri);
        return factory;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        return new RabbitTemplate(connectionFactory);
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public TaskQueue taskQueue(
            RabbitTemplate rabbitTemplate,
            RabbitAdmin rabbitAdmin,
            ObjectMapper objectMapper) {
        return new RabbitMQTaskQueue(rabbitTemplate, rabbitAdmin, objectMapper);
    }
}
