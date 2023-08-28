plugins {
	id("io.spring.dependency-management")
	id("java-library")
}

dependencies {
	api("org.springframework.boot:spring-boot")
	api("org.springframework.boot:spring-boot-autoconfigure")

	compileOnly("org.springframework:spring-webmvc")
	compileOnly("jakarta.servlet:jakarta.servlet-api")
	compileOnly("com.fasterxml.jackson.core:jackson-databind")
}

repositories {
	maven("https://repo.spring.io/milestone")
}