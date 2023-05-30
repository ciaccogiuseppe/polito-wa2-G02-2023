import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "3.0.5"
	id("io.spring.dependency-management") version "1.1.0"
	kotlin("jvm") version "1.7.22"
	kotlin("plugin.spring") version "1.7.22"
	kotlin("plugin.jpa") version "1.7.22"
	id("com.google.cloud.tools.jib") version "3.3.1"
}



group = "it.polito.wa2"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
	// implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.keycloak:keycloak-admin-client:21.0.1")
	implementation("org.jboss.resteasy:resteasy-client:3.1.3.Final")
	// implementation("org.keycloak:keycloak-spring-boot-starter")
	// compileOnly("org.projectlombok:lombok")
	// annotationProcessor("org.projectlombok:lombok")
	runtimeOnly("org.postgresql:postgresql")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	// testImplementation("org.keycloak:keycloak-test-helper")
	testImplementation ("org.testcontainers:junit-jupiter:1.16.3")
	testImplementation("org.testcontainers:postgresql:1.16.3")
	testImplementation("com.github.dasniko:testcontainers-keycloak:2.5.0")
	// annotationProcessor ("org.springframework.boot:spring-boot-configuration-processor")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "17"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.named("jibDockerBuild"){
	jib.container.ports = listOf("8080")
	jib.to.image = "ticketing"
}

dependencyManagement {
	imports {
		mavenBom("org.testcontainers:testcontainers-bom:1.16.3")
		// mavenBom("org.keycloak.bom:keycloak-adapter-bom:7.0.1")
	}
}
