dependencies{
    compileOnly(rootProject.libs.mapstruct.processor)
//    options.compilerArgs.add("-Amapstruct.accessorNamingStrategy=ru.leonaugust.link_aggregator.boot.config.mapstruct.CustomAccessorNamingStrategy")
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