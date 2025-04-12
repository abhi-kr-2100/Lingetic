import net.ltgt.gradle.errorprone.CheckSeverity
import net.ltgt.gradle.errorprone.errorprone

plugins {
	id("java-library")
	id("org.springframework.boot") version "3.4.2"
	id("io.spring.dependency-management") version "1.1.7"
	jacoco
	id("net.ltgt.errorprone") version "4.1.0"
	id("io.sentry.jvm.gradle") version "5.3.0"
}

group = "com.munetmo"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(23)
	}
}

repositories {
	mavenCentral()
}

buildscript {
	repositories {
		mavenCentral()
	}
}

extra["sentryVersion"] = "8.3.0"

val env: String? by project
val javaEnv = if (env == "development") "development" else "production"

ext {
	set("testcontainers.version", "1.20.6")
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("io.sentry:sentry-spring-boot-starter-jakarta")
	implementation("org.springframework.boot:spring-boot-starter-jdbc")
	implementation("org.flywaydb:flyway-database-postgresql")
	implementation("com.clerk:backend-api:1.5.0")
	implementation("org.springframework.boot:spring-boot-starter-amqp")
	runtimeOnly("org.postgresql:postgresql")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.boot:spring-boot-testcontainers")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	testImplementation("org.testcontainers:junit-jupiter")
	testImplementation("org.testcontainers:postgresql")
	testImplementation("org.testcontainers:rabbitmq")
	errorprone("com.google.errorprone:error_prone_core:2.36.0")
	errorprone("com.uber.nullaway:nullaway:0.12.3")
	api("org.jspecify:jspecify:1.0.0")
}

dependencyManagement {
	imports {
		mavenBom("io.sentry:sentry-bom:${property("sentryVersion")}")
	}
}


tasks.withType<Test> {
	useJUnitPlatform()
	finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
	dependsOn(tasks.test)
	reports {
		html.required.set(true)
	}
}

tasks.withType<JavaCompile>().configureEach {
	options.errorprone {
		check("NullAway", CheckSeverity.ERROR)
		option("NullAway:AnnotatedPackages", "com.munetmo")
	}
}

tasks.withType<JavaExec>().configureEach {
	jvmArgs("-DJAVA_ENV=$javaEnv")
}

val sentryAuthKey: String = System.getenv("SENTRY_AUTH_TOKEN") ?: throw IllegalStateException("SENTRY_AUTH_TOKEN environment variable is not set")

sentry {
	includeSourceContext.set(true)
	org.set("munetmo")
	projectName.set("lingetic-spring-backend")
	authToken.set(sentryAuthKey)
}
