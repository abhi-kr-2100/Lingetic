FROM amazoncorretto:23-jdk AS builder

WORKDIR /app

# Install findutils to provide xargs which is required by gradle but not provided by the base image
RUN yum update -y && yum install -y findutils

COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

COPY config/application.properties config/application.properties

COPY src/main src/main

RUN --mount=type=secret,id=SENTRY_AUTH_TOKEN \
    export SENTRY_AUTH_TOKEN=$(cat /run/secrets/SENTRY_AUTH_TOKEN) && \
    ./gradlew clean build -x test

FROM amazoncorretto:23-jdk

WORKDIR /app

COPY --from=builder /app/build/libs/lingetic-0.0.1-SNAPSHOT.jar application.jar
COPY --from=builder /app/config/application.properties config/application.properties

EXPOSE 8000

ENTRYPOINT ["java", "-jar", "application.jar"]
