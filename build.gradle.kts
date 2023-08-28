import org.gradle.api.Project
import java.io.ByteArrayOutputStream
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.jetbrains.kotlin.jvm") version "1.8.22" apply false
	id("org.springframework.boot") apply false
	id("io.spring.dependency-management") version "1.1.2"
	id("maven-publish")
}

fun Project.runCommand(command: String): String {
	val stdout = ByteArrayOutputStream()
	project.exec {
		commandLine = command.split(" ")
		standardOutput = stdout
	}

	return stdout
		.toString()
		.trim()
}

fun sanitizeVersion(branch: String): String {
	return branch
		.replace(Regex("[^A-Za-z0-9.-]+"), "-")
		.replace(Regex("-+"), "-")
}

fun Project.gitVersionPostfix(): String {
	val isGitRepo = runCatching { runCommand("git rev-parse --is-inside-work-tree") }
		.getOrElse { "" } == "true"

	if (!isGitRepo) {
		return "-nogit"
	}

	val branch = runCommand("git rev-parse --abbrev-ref HEAD")
	val commit = runCommand("git rev-parse --short HEAD")
	return sanitizeVersion("-$branch-$commit")
}

allprojects {
	apply {
		plugin("maven-publish")
		plugin("io.spring.dependency-management")
	}

	version = "0.6.0" + gitVersionPostfix()
	group = "org.springframework.fu"

	dependencyManagement {
		imports {
			mavenBom("org.springframework.boot:spring-boot-dependencies:${findProperty("bootVersion")}")
			mavenBom("org.testcontainers:testcontainers-bom:1.15.3")
		}
	}

	tasks.withType<Test> {
		useJUnitPlatform()
	}

	tasks.withType<JavaCompile> {
		sourceCompatibility = "17"
		targetCompatibility = "17"
	}

	tasks.withType<KotlinCompile> {
		kotlinOptions {
			freeCompilerArgs += "-Xjsr305=strict"
			freeCompilerArgs += "-Xextended-compiler-checks"
			jvmTarget = "17"
		}
	}

	repositories {
		mavenCentral()
		maven("https://repo.spring.io/milestone")
		maven("https://repo.spring.io/snapshot")
	}
}

fun CopySpec.setExecutablePermissions() {
	filesMatching("gradlew") { mode = 0b111101101 }
	filesMatching("gradlew.bat") { mode = 0b110100100 }
}
