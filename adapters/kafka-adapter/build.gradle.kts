dependencies{
    annotationProcessor(project(":link-aggregator-build-tools"))

    implementation(project(":core:link-aggregator-core-application"))
    implementation("org.springframework.boot:spring-boot-starter-kafka")

}

tasks.publish {
    enabled = false
}

tasks.publishToMavenLocal {
    enabled = false
}

tasks.withType<PublishToMavenRepository> {
    enabled = false
}

tasks.withType<PublishToMavenLocal> {
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