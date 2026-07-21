plugins {
  id("jacoco-report-aggregation")
  alias(libs.plugins.sonar)
}

val allAnalyzedProjects = rootProject.subprojects
  .filter { subProject -> subProject.name != project.name }
  .filter { subProject -> subProject.plugins.hasPlugin("java-library") }
  .filter { it.path !in listOf(":adapters", ":core") }

allAnalyzedProjects
  .forEach { subProject -> dependencies.add("implementation", subProject) }

val binaries = allAnalyzedProjects.joinToString(",") { it.projectDir.absolutePath.plus("/build/classes/java/main") }
val testBinaries = allAnalyzedProjects.joinToString(",") { it.projectDir.absolutePath.plus("/build/classes/java/test") }
val projectSources = allAnalyzedProjects.joinToString(",") { it.projectDir.absolutePath.plus("/src/main/java/") }
val sonarExclusions = listOf(
  "**/ru/leonaugust/link_aggregator/boot/**/*Aggregate.*",
  "**/ru/leonaugust/link_aggregator/boot/**/*AggregateRoot.*",
  "**/ru/leonaugust/link_aggregator/boot/**/*Model.*",
  "**/ru/leonaugust/link_aggregator/boot/**/*ValueObject.*",
  "**/ru/leonaugust/link_aggregator/boot/**/*Dto.*",
  "**/ru/leonaugust/link_aggregator/boot/**/*DTO.*",
  "**/ru/leonaugust/link_aggregator/boot/**/*Key.*",
  "**/ru/leonaugust/link_aggregator/boot/**/*Enum.*",
  "**/ru/leonaugust/link_aggregator/boot/**/*Entity.*",
  "**/ru/leonaugust/link_aggregator/boot/**/*Embeddable.*",
  "**/ru/leonaugust/link_aggregator/boot/**/*Aspect.*",
  "**/ru/leonaugust/link_aggregator/boot/**/*Configuration*.*",
  "**/ru/leonaugust/link_aggregator/boot/**/*Exception.*",
  "**/ru/leonaugust/link_aggregator/boot/**/*Config*.*",
  "**/ru/leonaugust/link_aggregator/boot/**/*Constant.*",
  "**/ru/leonaugust/link_aggregator/boot/**/*FieldHolder.*",
  "**/ru/leonaugust/link_aggregator/boot/**/*Abstract*.*",
  "**/ru/leonaugust/link_aggregator/boot/**/constant/*.*",
  "**/ru/leonaugust/link_aggregator/boot/**/*Builder.*",
  "**/ru/leonaugust/link_aggregator/boot/**/*Application.*",
  "**/ru/leonaugust/link_aggregator/boot/**/*Starter.*",
  "**/ru/leonaugust/link_aggregator/boot/**/*Tracer.*",
  "**/ru/leonaugust/link_aggregator/boot/**/*AjcClosure*",
  "**/*.factories",
  "**/*.yaml",
  "**/*.yml",
  "**/*.sql",
  "**/*.csv",
  "**/*.json",
  "**/*.xml",
  "**/*.properties",
  "**/*.mustache"
).joinToString(",")

sonar {
  properties {
    property("sonar.projectKey", "link-aggregator")
    property("sonar.host.url", "http://localhost:9000")
    property("sonar.login", "admin")
    property("sonar.password", "admin1")
    property("sonar.java.coveragePlugin", "jacoco")
    property("sonar.coverage.jacoco.xmlReportPaths",
      "${rootDir}/jacoco-aggregator/build/jacocoReports/jacocoRootReport/jacocoRootReport.xml")
    property("sonar.java.binaries", binaries)
    property("sonar.sources", projectSources)
    property("sonar.java.test.binaries", testBinaries)
    property("sonar.projectBaseDir", rootProject.projectDir.absolutePath)
//    property("sonar.sources",
//      subprojects
//        .filter { it.path !in listOf(":adapters", ":core") }
//        .flatMap { it.sourceSets["main"].allSource.srcDirs }
//        .joinToString(",")
//    )
//    property("sonar.java.binaries",
//      subprojects
//        .filter { it.path !in listOf(":adapters", ":core") }
//        .flatMap { it.sourceSets["main"].output.classesDirs }
//        .joinToString(",")
//    )
  }
}

tasks.check {
  finalizedBy(tasks.testCodeCoverageReport)
}

tasks.testCodeCoverageReport {

  val allCheckTasks = allAnalyzedProjects
    .map { p -> p.tasks.check }
  val allExecutionData = allAnalyzedProjects
    .map { p -> p.fileTree(p.projectDir.absolutePath.plus("/build/jacoco")) }
  val allAnalyzedClassDirectories = allAnalyzedProjects
    .map { it.tasks.jacocoTestReport.get().classDirectories.files }

  val classFiles = allAnalyzedClassDirectories.stream()
    .flatMap { it.stream() }
    .filter { it.absolutePath.endsWith(".class") }
    .toList()

  enabled = true
  dependsOn(allCheckTasks)
  executionData(allExecutionData)
  additionalClassDirs.setFrom(classFiles)

  reports {
    xml.required.set(true)
    html.required.set(true)
    csv.required.set(false)
  }
  doLast {
    val reportFile = layout.buildDirectory
      .file("jacocoReports/testCodeCoverageReport/html/index.html")
      .get().asFile
    val reportUri = reportFile.toURI().toString()
    logger.lifecycle(
      "\n=== JaCoCo Aggregated Report ===\n" +
              "📊 HTML: $reportUri\n"
    )
  }
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