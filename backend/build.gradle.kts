plugins {
    `java-test-fixtures`
    kotlin("jvm") version "2.4.0"
    kotlin("plugin.spring") version "2.4.0"
    id("idea")
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    id("org.owasp.dependencycheck") version "12.1.8"
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
    extendsFrom(
        configurations.implementation.get(),
        configurations.testFixturesImplementation.get(),
        configurations.testImplementation.get()
    )
}
val integrationTestRuntimeOnly by configurations.getting

configurations["integrationTestRuntimeOnly"].extendsFrom(
    configurations.runtimeOnly.get(),
    configurations.testRuntimeOnly.get()
)

idea {
    module {
        testSources.from(sourceSets["integrationTest"].kotlin.srcDirs)
        testResources.from(sourceSets["integrationTest"].resources.srcDirs)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("org.springframework.boot:spring-boot-starter-liquibase")
    implementation(libs.exposed.spring.boot)
    implementation(libs.exposed.java.time)
    implementation(libs.uuid.creator)
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    runtimeOnly("org.postgresql:postgresql")

    testImplementation(libs.assertk.jvm)
    testImplementation(libs.mockk)
    testImplementation(libs.mockk.agent)
    testImplementation(kotlin("test"))

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

    integrationTestImplementation(platform(libs.test.containers.bom))
    integrationTestImplementation("org.springframework.boot:spring-boot-starter-test") { exclude(group = "org.mockito") }
    integrationTestImplementation("org.springframework.boot:spring-boot-restclient-test")
    integrationTestImplementation("org.junit.jupiter:junit-jupiter")
    integrationTestImplementation("org.springframework.boot:spring-boot-testcontainers")
    integrationTestImplementation("org.testcontainers:testcontainers")
    integrationTestImplementation("org.testcontainers:testcontainers-postgresql")
    integrationTestImplementation("org.testcontainers:testcontainers-junit-jupiter")

    integrationTestRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

allOpen {
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.register<Test>("integrationTest") {
    description = "Runs integration tests."
    group = "verification"
    testClassesDirs = sourceSets.getByName("integrationTest").output.classesDirs
    classpath = sourceSets.getByName("integrationTest").runtimeClasspath.filter {
        !it.path.contains("/build/libs/")
    }
    useJUnitPlatform()
    environment("LIQUIBASE_DUPLICATE_FILE_MODE", "SILENT")
}

tasks.named("check") {
    dependsOn(tasks.named("integrationTest"))
}
