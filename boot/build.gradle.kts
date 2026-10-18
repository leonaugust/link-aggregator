import org.springframework.boot.gradle.tasks.bundling.BootJar
import org.springframework.boot.gradle.tasks.run.BootRun

plugins {
    alias(libs.plugins.spring.boot)
}

dependencies {
    implementation("org.springframework.boot:spring-boot-autoconfigure")
    implementation(project(":core:link-aggregator-core-application"))

    implementation(project(":adapters:link-aggregator-ai-adapter"))
    implementation(project(":adapters:link-aggregator-fraud-adapter"))
    implementation(project(":adapters:link-aggregator-postgres-adapter"))
    runtimeOnly(project(":adapters:link-aggregator-kafka-adapter"))
    runtimeOnly(project(":adapters:link-aggregator-util"))
    runtimeOnly(project(":adapters:link-aggregator-rest-adapter"))
    runtimeOnly(project(":adapters:link-aggregator-email-adapter"))
    runtimeOnly(project(":adapters:link-aggregator-html-fetcher-adapter"))
    implementation("org.hibernate.orm:hibernate-core")

    implementation("org.springframework.boot:spring-boot-starter")

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("org.springframework.boot:spring-boot-starter-web")
    runtimeOnly(rootProject.libs.postgres)

    implementation("org.springframework.boot:spring-boot-starter-webflux")

    testImplementation("org.springframework.boot:spring-boot-starter-mail")
    testImplementation("org.springframework.boot:spring-boot-autoconfigure")
    testImplementation(rootProject.libs.testcontainers)
    testImplementation(rootProject.libs.testcontainers.junit)
    testImplementation(rootProject.libs.testcontainers.postgres)
    testImplementation(rootProject.libs.spring.cloud.stub.runner)
    testImplementation(rootProject.libs.gson)
    testImplementation("org.springframework.kafka:spring-kafka")
    testImplementation("org.springframework.kafka:spring-kafka-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa")

    testImplementation(project(":core:link-aggregator-core-application"))
    testImplementation(project(":adapters:link-aggregator-postgres-adapter"))
    testImplementation(project(":adapters:link-aggregator-rest-adapter"))
    testImplementation(project(":adapters:link-aggregator-ai-adapter"))
    testImplementation(project(":adapters:link-aggregator-html-fetcher-adapter"))
    testImplementation("org.testcontainers:ollama:1.20.4")
//    testImplementation("org.springframework.ai:spring-ai-ollama-spring-boot-starter:1.0.0-M6")
}

springBoot {
    buildInfo()
}

tasks.build {
    dependsOn("bootJar")
}

tasks.named<BootRun>("bootRun") {
    jvmArgs = listOf("-Duser.timezone=UTC", "-Dspring.profiles.active=local")
}

tasks.named<BootJar>("bootJar") {
    archiveClassifier.set("boot")
    mainClass.set("ru.leonaugust.link_aggregator.boot.MicroserviceStarter")
    manifest {
        attributes(
            "Implementation-Version" to project.version
        )
    }
}

tasks.build {
    dependsOn(tasks.bootJar)
}

tasks.publish {
    enabled = false
}

tasks.publishToMavenLocal {
    enabled = false
}

tasks.withType<PublishToMavenRepository>().configureEach {
    enabled = false
}

tasks.withType<PublishToMavenLocal>().configureEach {
    enabled = false
}

tasks.named<Jar>("jar") {
    enabled = false
}

tasks.named<Jar>("javadocJar") {
    enabled = false
}

tasks.named<Jar>("sourcesJar") {
    enabled = false
}

tasks.named<Javadoc>("javadoc") {
    enabled = false
}