package com.munetmo.lingetic.infra.database;

import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class PostgresDatabaseConfig {
    @Value("${spring.datasource.url}")
    @Nullable
    private String url;

    @Value("${spring.datasource.username}")
    @Nullable
    private String username;

    @Value("${spring.datasource.password}")
    @Nullable
    private String password;

    @Value("${spring.datasource.driver-class-name}")
    @Nullable
    private String driverClassName;

    @Bean
    public DataSource dataSource() {
        if (url == null || username == null || password == null || driverClassName == null) {
            throw new IllegalStateException("Database configuration is not set");
        }

        return DataSourceBuilder.create()
                .url(url)
                .username(username)
                .password(password)
                .driverClassName(driverClassName)
                .build();
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
