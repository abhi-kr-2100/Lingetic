package com.munetmo.lingetic.lib;

import com.munetmo.lingetic.lib.environment.EnvironmentService;
import io.sentry.Sentry;
import io.sentry.SentryLevel;

public class Utilities {
    public enum Severity {
        FATAL,
        ERROR,
        WARNING,
        INFO
    }

    public static void assert_(boolean condition, String message) {
        if (condition) {
            return;
        }

        log(message, Severity.FATAL);
        throw new RuntimeException("Assertion failed.");
    }

    public static void log(String message, Severity severity) {
        var environment = EnvironmentService.getEnvironment();

        switch (severity) {
            case Severity.FATAL:
            case Severity.ERROR:
                if (environment == EnvironmentService.Environment.PRODUCTION) {
                    Sentry.captureMessage(message, switch (severity) {
                        case Severity.FATAL -> SentryLevel.FATAL;
                        case Severity.ERROR -> SentryLevel.ERROR;
                        default -> throw new IllegalStateException("Unexpected value: " + severity);
                    });
                }
                System.err.println(message);
                break;
            case Severity.WARNING:
                System.err.println(message);
                break;
            case Severity.INFO:
                System.out.println(message);
                break;
        }
    }
}
