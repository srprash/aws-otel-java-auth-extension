plugins {
    java
    id("org.springframework.boot") version "3.2.3"
    id("io.spring.dependency-management") version "1.1.4"
}

group = "com.amazon.sampleapp"
version = "0.1.0-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("software.amazon.awssdk:s3:2.25.10")
    implementation("software.amazon.awssdk:sts:2.25.10")
    implementation("io.opentelemetry:opentelemetry-api:1.32.0")
    
    // Add the AWS OpenTelemetry Java Auth Extension
    // implementation("software.amazon.opentelemetry:aws-otel-java-auth-extension:1.0-SNAPSHOT")
    
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
