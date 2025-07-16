plugins {
    id("java")
    id("maven-publish")
}

group = "software.amazon.opentelemetry"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // OTel dependencies
    implementation("io.opentelemetry:opentelemetry-sdk:1.52.0")
    implementation("io.opentelemetry:opentelemetry-exporter-otlp:1.52.0") {
        exclude(group = "io.opentelemetry", module = "opentelemetry-exporter-sender-okhttp")
    }
    implementation("io.opentelemetry:opentelemetry-exporter-otlp-common:1.52.0")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    implementation("software.amazon.awssdk:auth:2.25.10")
    implementation("software.amazon.awssdk:core:2.25.10")
    implementation("software.amazon.awssdk:regions:2.25.10")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = "software.amazon.opentelemetry" // or your group
            artifactId = "aws-otel-java-auth-extension"
            version = "1.0-SNAPSHOT"
        }
    }
}