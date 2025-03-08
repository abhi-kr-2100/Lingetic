package com.munetmo.lingetic.lib.environment;

public class EnvironmentService {
    public enum Environment {
        PRODUCTION,
        DEVELOPMENT
    }

    public static Environment getEnvironment() {
        var javaEnv = System.getProperty("JAVA_ENV");
        if (javaEnv == null) {
            throw new IllegalStateException("JAVA_ENV environment variable is not set");
        }

        return switch (javaEnv) {
            case "production" -> Environment.PRODUCTION;
            case "development" -> Environment.DEVELOPMENT;
            default -> throw new IllegalArgumentException("Invalid active profile: " + javaEnv);
        };
    }
}
