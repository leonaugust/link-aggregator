import org.gradle.internal.execution.caching.CachingState.enabled

dependencies{
    annotationProcessor(project(":link-aggregator-build-tools"))

    implementation(project(":core:link-aggregator-core-application"))
    implementation("org.springframework:spring-context")
    implementation(rootProject.libs.jsoup)
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