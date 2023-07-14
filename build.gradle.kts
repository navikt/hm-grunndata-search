import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.internal.execution.history.changes.ExecutionStateChanges.incremental
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


val jvmTarget = "17"
val micronautVersion="3.9.4"
val micrometerRegistryPrometheusVersion = "1.9.1"
val junitJupiterVersion = "5.9.0"
val jacksonVersion = "2.13.4"
val logbackClassicVersion = "1.4.7"
val logbackEncoderVersion = "7.3"
val mockkVersion = "1.13.4"
val kotestVersion = "5.5.5"
val openSearchRestClientVersion = "2.4.1"

group = "no.nav.hm"
version = properties["version"] ?: "local-build"

plugins {
    kotlin("jvm") version "1.7.0"
    kotlin("kapt") version "1.7.0"
    id("java")
    id("com.github.johnrengelman.shadow") version "7.1.0"
    id("io.micronaut.application") version "3.7.10"
}

configurations.all {
    resolutionStrategy {
        failOnChangingVersions()
    }
}

dependencies {
    api("ch.qos.logback:logback-classic:$logbackClassicVersion")
    api("net.logstash.logback:logstash-logback-encoder:$logbackEncoderVersion")
    // coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("io.micronaut:micronaut-runtime")
    implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")
    implementation("io.micronaut:micronaut-http-server-netty")
    implementation("io.micronaut:micronaut-http-client")
    implementation("org.opensearch.client:opensearch-rest-high-level-client:${openSearchRestClientVersion}")
    implementation("io.micronaut.micrometer:micronaut-micrometer-core")
    implementation("io.micronaut.micrometer:micronaut-micrometer-registry-prometheus")
    implementation("io.micronaut:micronaut-management")

    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("io.kotest:kotest-runner-junit5-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core-jvm:$kotestVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.9.2")
}

micronaut {
    version.set(micronautVersion)
    testRuntime("netty")
    testRuntime("junit5")
    processing {
        incremental(true)
    }
}

application {
    mainClass.set("no.nav.hm.grunndata.search.Application")
}

java {
    sourceCompatibility = JavaVersion.toVersion(jvmTarget)
    targetCompatibility = JavaVersion.toVersion(jvmTarget)
    withSourcesJar()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = jvmTarget
    kapt.includeCompileClasspath = false
}

tasks.named<KotlinCompile>("compileTestKotlin") {
    kotlinOptions.jvmTarget = jvmTarget
    kapt.includeCompileClasspath = false
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("skipped", "failed")
        showExceptions = true
        showStackTraces = true
        showCauses = true
        exceptionFormat = TestExceptionFormat.FULL
        showStandardStreams = true
    }
}

tasks.withType<Wrapper> {
    gradleVersion = "8.0.1"
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://packages.confluent.io/maven/")

}

