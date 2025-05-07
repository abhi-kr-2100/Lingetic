FROM gradle:jdk23-graal AS builder

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

RUN --mount=type=secret,id=SENTRY_AUTH_TOKEN \
    export SENTRY_AUTH_TOKEN=$(cat /run/secrets/SENTRY_AUTH_TOKEN) && \
    ./gradlew dependencies --no-daemon

COPY src/main src/main
COPY src/main/resources/dictionaries/system_full.dic src/main/resources/dictionaries/system_full.dic

RUN --mount=type=secret,id=SENTRY_AUTH_TOKEN \
    export SENTRY_AUTH_TOKEN=$(cat /run/secrets/SENTRY_AUTH_TOKEN) && \
    ./gradlew nativeCompile -x test --no-daemon

FROM ubuntu:latest AS runtime

WORKDIR /app

COPY --from=builder /app/build/native/nativeCompile/lingetic ./lingetic
COPY config/application.properties config/application.properties

EXPOSE 8000

ENTRYPOINT ["./lingetic"]
