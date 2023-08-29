import org.gradle.api.Project
import java.io.ByteArrayOutputStream
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.api.publish.maven.MavenPublication

plugins {
	id("org.jetbrains.kotlin.jvm") version "1.8.22" apply false
	id("org.springframework.boot") apply false
	id("io.spring.dependency-management") version "1.1.2"
	id("maven-publish")
	id("com.jfrog.artifactory") version "5.+"
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
	val commitsCount = runCommand("git rev-list --count HEAD")
	return sanitizeVersion("-$branch-$commitsCount")
}

fun String.kebabToLowerCamelCase(): String {
	return "-[a-zA-Z]"
		.toRegex()
		.replace(this) {
			it.value
				.replace("-","")
				.toUpperCase()
		}
}

subprojects {
	apply {
		plugin("java-library")
		plugin("maven-publish")
		plugin("com.jfrog.artifactory")
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

	tasks.register<Jar>("sourcesJar") {
		dependsOn("classes")
		archiveClassifier.set("sources")

		from(project.the<SourceSetContainer>()["main"].allSource)
	}

	tasks.register<Jar>("javadocJar") {
		dependsOn("javadoc")
		archiveClassifier.set("javadoc")

		from(tasks.named<Javadoc>("javadoc").get().destinationDir)
	}

	artifactory {
		clientConfig.isIncludeEnvVars = false

		val artifactoryContextUrl: String = findProperty("artifactoryContextUrl")
			?.toString()
			?: error(
				"Artifactory Context Url must not be null. " +
				"Check artifactoryContextUrl in gradle.properties or environment"
			)

		val artifactoryRepoPublish: String = findProperty("artifactoryRepoPublish")
			?.toString()
			?: error(
				"Artifactory Repo Publish must not be null. " +
				"Check artifactoryRepoPublish in gradle.properties or environment"
			)

		val artifactoryUser: String = findProperty("artifactoryUser")
			?.toString()
			?: error(
				"Artifactory User must not be null. " +
				"Check artifactoryUser in gradle.properties or environment"
			)

		val artifactoryPassword: String = findProperty("artifactoryPassword")
			?.toString()
			?: error(
				"Artifactory Password must not be null. " +
				"Check artifactoryPassword in gradle.properties or environment"
			)

		setContextUrl(artifactoryContextUrl)

		publish {
			repository {
				repoKey = artifactoryRepoPublish
				username = artifactoryUser
				password = artifactoryPassword
			}

			defaults {
				publications(project.name.kebabToLowerCamelCase())
				setPublishArtifacts(true)
				isPublishBuildInfo = false
			}
		}
	}

	publishing {
		publications {
			create<MavenPublication>(project.name.kebabToLowerCamelCase()) {
				groupId = project.group.toString()
				version = project.version.toString()
				artifactId = project.name

				versionMapping {
					usage("java-api") {
						fromResolutionOf("runtimeClasspath")
					}

					usage("java-runtime") {
						fromResolutionResult()
					}
				}

				artifact(tasks.named("sourcesJar"))
				artifact(tasks.named("javadocJar"))
				from(components["java"])
			}
		}
	}
}
