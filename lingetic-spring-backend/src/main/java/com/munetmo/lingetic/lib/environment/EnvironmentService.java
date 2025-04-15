package com.munetmo.lingetic.lib.environment;

public class EnvironmentService {
    public enum Environment {
        PRODUCTION,
        DEVELOPMENT
    }

    public static Environment getEnvironment() {
        var environment = System.getenv("ENVIRONMENT");
        if (environment == null) {
            throw new IllegalStateException("ENVIRONMENT environment variable is not set");
        }

        return switch (environment) {
            case "production" -> Environment.PRODUCTION;
            case "development" -> Environment.DEVELOPMENT;
            default -> throw new IllegalArgumentException("Invalid active profile: " + environment);
        };
    }
}
