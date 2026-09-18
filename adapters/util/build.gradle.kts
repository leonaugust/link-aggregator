dependencies{

  implementation("org.springframework.boot:spring-boot-starter-aop:3.5.16")
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