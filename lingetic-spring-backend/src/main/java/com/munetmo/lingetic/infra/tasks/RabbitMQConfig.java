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
    @Value("${spring.rabbitmq.host}")
    @Nullable
    private String host;

    @Value("${spring.rabbitmq.port}")
    private int port;

    @Value("${spring.rabbitmq.username}")
    @Nullable
    private String username;

    @Value("${spring.rabbitmq.password}")
    @Nullable
    private String password;

    @Bean
    public ConnectionFactory connectionFactory() {
        Utilities.assert_(host != null && !host.isBlank(), "RabbitMQ host cannot be null or empty");
        Utilities.assert_(username != null && !username.isBlank(), "RabbitMQ username cannot be null or empty");
        Utilities.assert_(password != null && !password.isBlank(), "RabbitMQ password cannot be null or empty");
        Utilities.assert_(port > 0, "RabbitMQ port must be greater than 0");

        var factory = new CachingConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        factory.setUsername(username);
        factory.setPassword(password);
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
