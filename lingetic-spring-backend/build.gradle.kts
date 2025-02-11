import net.ltgt.gradle.errorprone.CheckSeverity
import net.ltgt.gradle.errorprone.errorprone

plugins {
	id("java-library")
	id("org.springframework.boot") version "3.4.2"
	id("io.spring.dependency-management") version "1.1.7"
	jacoco
	id("net.ltgt.errorprone") version "4.1.0"
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

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	errorprone("com.google.errorprone:error_prone_core:2.36.0")
	errorprone("com.uber.nullaway:nullaway:0.12.3")
	api("org.jspecify:jspecify:1.0.0")
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