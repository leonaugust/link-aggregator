dependencies {
    api(project(":core:link-aggregator-core-domain"))
    api("io.projectreactor:reactor-core:3.8.5")
    implementation(rootProject.libs.mapstruct)
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