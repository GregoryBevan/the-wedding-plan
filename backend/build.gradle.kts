plugins {
    `java-test-fixtures`
    kotlin("jvm") version "2.4.0"
    kotlin("plugin.spring") version "2.4.0"
    id("org.springframework.boot") version "4.0.6"
    id("io.spring.dependency-management") version "1.1.4"
    id("idea")
}


group = "me.elgregoss"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}
sourceSets {
    create("integrationTest") {
        compileClasspath += sourceSets.main.get().output + sourceSets.testFixtures.get().output
        runtimeClasspath += sourceSets.main.get().output + sourceSets.testFixtures.get().output + sourceSets.test.get().output
    }
}

val integrationTestImplementation by configurations.getting {
    extendsFrom(configurations.implementation.get(), configurations.testFixturesImplementation.get(), configurations.testImplementation.get())
}
val integrationTestRuntimeOnly by configurations.getting

configurations["integrationTestRuntimeOnly"].extendsFrom(configurations.runtimeOnly.get(),configurations.testRuntimeOnly.get())

repositories {
    mavenCentral()
}

extra["testcontainersVersion"] = "2.0.5"

dependencyManagement {
    imports {
        mavenBom("org.testcontainers:testcontainers-bom:${property("testcontainersVersion")}")
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-liquibase")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    runtimeOnly("org.postgresql:postgresql")
    
    // Kotlin specific dependency for stdlib and runtime
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:0.28.1")
    testImplementation("io.mockk:mockk:1.13.16")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

    integrationTestImplementation("org.springframework.boot:spring-boot-starter-test")
    integrationTestImplementation("org.junit.jupiter:junit-jupiter")
    integrationTestImplementation("org.springframework.boot:spring-boot-testcontainers")
    integrationTestImplementation("org.testcontainers:testcontainers")
    integrationTestImplementation("org.testcontainers:testcontainers-postgresql")
    integrationTestImplementation("org.testcontainers:testcontainers-junit-jupiter")
    integrationTestRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.register<Test>("integrationTest") {
    description = "Runs integration tests."
    group = "verification"
    testClassesDirs = sourceSets.getByName("integrationTest").output.classesDirs
    classpath = sourceSets.getByName("integrationTest").runtimeClasspath
    useJUnitPlatform()
}
