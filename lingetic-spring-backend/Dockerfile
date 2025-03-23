FROM amazoncorretto:23-jdk

WORKDIR /app

COPY build/libs/lingetic-0.0.1-SNAPSHOT.jar app.jar
COPY config/application-production.properties config/application.properties

EXPOSE 8000

ENTRYPOINT ["java", "-jar", "app.jar"]
