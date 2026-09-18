dependencies{
    annotationProcessor(project(":link-aggregator-build-tools"))
    api(project(":adapters:link-aggregator-util"))

    implementation(project(":core:link-aggregator-core-application"))
    api("org.springframework.boot:spring-boot-starter-web")
    implementation(rootProject.libs.swagger.core.jakarta)
    implementation(rootProject.libs.swagger.annotations.jakarta)
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

tasks.named<Jar>("javadocJar") {
    enabled = false
}

tasks.named<Jar>("sourcesJar") {
    enabled = false
}

tasks.named<Javadoc>("javadoc") {
    enabled = false
}