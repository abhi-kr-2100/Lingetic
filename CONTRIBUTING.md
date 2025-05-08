# Contributing

`Lingetic` is a language learning app.

## Technologies

### Backend

- Gradle using Kotlin DSL
- Java 23 using Spring Boot
- GraalVM
- Entities are just Java classes
- Repositories are manually coded to map from rows to objects
- PostgreSQL with Flyway for migrations
- Docker for containerized PostgreSQL and RabbitMQ
- JUnit Jupiter for unit tests
- Testcontainers for testing with real PostgreSQL and RabbitMQ
- No ORM
- No mocking

### Frontend

- pnpm
- Next.js 15 using the App Router
- Tailwind CSS
- Lucide
- Clerk is used for authentication
- jest for testing

### Miscellaneous

- Python utility scripts
- uv for Python
- devenv for local development environment

## Files

- Backend: `lingetic-spring-backend/`

  - Spring Boot config: `lingetic-spring-backend/config/application.properties`
  - Main backend source files: `lingetic-spring-backend/src/main/java/com/munetmo/lingetic`
  - Main backend test files: `lingetic-spring-backend/src/test/java/com/munetmo/lingetic`
  - Tests for file `lingetic-spring-backend/src/main/java/com/munetmo/lingetic/X/Y.java` are in `lingetic-spring-backend/src/test/java/com/munetmo/lingetic/X/YTest.java`
  - Migrations: `lingetic-spring-backend/src/main/resources/db/migration`
  - Language models: `lingetic-spring-backend/src/main/java/com/munetmo/lingetic/LanguageService/Entities/LanguageModels`
  - Supported languages: `lingetic-spring-backend/src/main/java/com/munetmo/lingetic/LanguageService/Entities/Language.java`
  - Question types: `lingetic-spring-backend/src/main/java/com/munetmo/lingetic/LanguageTestService/Entities/Questions/`; `lingetic-spring-backend/src/main/java/com/munetmo/lingetic/LanguageTestService/DTOs/Question`
  - Each question type has an associated AttemptRequest type and an AttemptResponse type: `lingetic-spring-backend/src/main/java/com/munetmo/lingetic/LanguageTestService/DTOs/Attempt/AttemptRequests`; `lingetic-spring-backend/src/main/java/com/munetmo/lingetic/LanguageTestService/DTOs/Attempt/AttemptResponses`

- Frontend: `lingetic-nextjs-frontend/`
- Python scripts: `scripts/`
  - All scripts are run from `scripts/main.py`; scripts are not run directly. So, the scripts must have a `get_parser` function, and a main function whose arguments exactly match the names of the command-line arguments.

## Commands

- Test backend: `./gradlew test` inside `lingetic-spring-backend/`
- Test frontend: `pnpm test` inside `lingetic-nextjs-frontend/`
- Load docker and ollama processes: `devenv up -d` inside the root directory
- Run a script: `uv run python main.py script_name script_args...` inside `scripts/`
- Run the frontend: `pnpm dev` inside `lingetic-nextjs-frontend/`
- Start containerized services: `docker compose up -d` inside `lingetic-spring-backend/`
- Build an image of the backend: `DOCKER_BUILDKIT=1 docker buildx build --secret id=SENTRY_AUTH_TOKEN,env=SENTRY_AUTH_TOKEN -t lingetic:latest .` inside `lingetic-spring-backend/`
- Run the backend image: `docker run -p 8000:8000 --env-file ../.env -e DATABASE_HOST=postgres -e RABBITMQ_HOST=rabbitmq --network lingetic-spring-backend_default lingetic`
- Tag the docker image: `docker tag lingetic:latest europe-west3-docker.pkg.dev/lingetic/lingetic-spring-backend/lingetic:latest` inside `lingetic-spring-backend/`
- Push the docker image: `docker push europe-west3-docker.pkg.dev/lingetic/lingetic-spring-backend/lingetic:latest` inside `lingetic-spring-backend/`
- Run the backend without docker: `./gradlew bootRun` inside `lingetic-spring-backend/`
- Create a Jar of the backend: `./gradlew bootJar` inside `lingetic-spring-backend/`
- Collect reachability metadata for GraalVM: `java -agentlib:native-image-agent=config-merge-dir=src/main/resources/META-INF/native-image/com.munetmo/lingetic -jar build/libs/lingetic-0.0.1-SNAPSHOT.jar`

## Supporting a new language

- Add it to `lingetic-spring-backend/src/main/java/com/munetmo/lingetic/LanguageService/Entities/Language.java`
- Add a new language model in `lingetic-spring-backend/src/main/java/com/munetmo/lingetic/LanguageService/Entities/LanguageModels`
- Add tests for the new language model in `lingetic-spring-backend/src/test/java/com/munetmo/lingetic/LanguageService/LanguageServiceTest.java`
- Add a migration file to support the new language in `lingetic-spring-backend/src/main/resources/db/migration`
- Enable the new language in `lingetic-nextjs-frontend/app/languages/constants.ts`

## Supporting a new question type

- Add a new question type in `lingetic-spring-backend/src/main/java/com/munetmo/lingetic/LanguageTestService/Entities/Questions`
- Add new tests for the new question type in `lingetic-spring-backend/src/test/java/com/munetmo/lingetic/LanguageTestService/Entities/Questions`
- Add new DTOs, AttemptResponse, and AttemptRequest types in `lingetic-spring-backend/src/main/java/com/munetmo/lingetic/LanguageTestService/DTOs/Question`, `lingetic-spring-backend/src/main/java/com/munetmo/lingetic/LanguageTestService/DTOs/Attempt/AttemptRequests`, and `lingetic-spring-backend/src/main/java/com/munetmo/lingetic/LanguageTestService/DTOs/Attempt/AttemptResponses`
- Add new tests for the new DTOs in `lingetic-spring-backend/src/test/java/com/munetmo/lingetic/LanguageTestService/DTOs/Question`, `lingetic-spring-backend/src/test/java/com/munetmo/lingetic/LanguageTestService/DTOs/Attempt/AttemptRequests`, and `lingetic-spring-backend/src/test/java/com/munetmo/lingetic/LanguageTestService/DTOs/Attempt/AttemptResponses`
- Update AttemptQuestionUseCase: `lingetic-spring-backend/src/main/java/com/munetmo/lingetic/LanguageTestService/UseCases/AttemptQuestionUseCase.java`
- Update tests for AttemptQuestionUseCase in `lingetic-spring-backend/src/test/java/com/munetmo/lingetic/LanguageTestService/UseCases/AttemptQuestionUseCaseTest.java`
- Update QuestionPostgresRepository: `lingetic-spring-backend/src/main/java/com/munetmo/lingetic/LanguageTestService/infra/Repositories/Postgres/QuestionPostgresRepository.java`
- Update tests for QuestionPostgresRepository in `lingetic-spring-backend/src/test/java/com/munetmo/lingetic/LanguageTestService/infra/Repositories/Postgres/QuestionPostgresRepositoryTest.java`
