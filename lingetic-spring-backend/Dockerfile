FROM gradle:jdk23-alpine AS builder

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

RUN --mount=type=secret,id=SENTRY_AUTH_TOKEN \
    export SENTRY_AUTH_TOKEN=$(cat /run/secrets/SENTRY_AUTH_TOKEN) && \
    ./gradlew dependencies --no-daemon

COPY src/main src/main

RUN --mount=type=secret,id=SENTRY_AUTH_TOKEN \
    export SENTRY_AUTH_TOKEN=$(cat /run/secrets/SENTRY_AUTH_TOKEN) && \
    ./gradlew build -x test --no-daemon

FROM amazoncorretto:23-jdk AS runtime

WORKDIR /app

COPY --from=builder /app/build/libs/lingetic-0.0.1-SNAPSHOT.jar application.jar
COPY config/application.properties config/application.properties

EXPOSE 8000

ENTRYPOINT ["java", "-jar", "application.jar"]
