import java.util.Properties

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

val sonarJacocoXmlReport = layout.buildDirectory
  .file("jacocoReports/testCodeCoverageReport/testCodeCoverageReport.xml")

val projectLocalSonarProps: Properties? = rootProject.projectDir
  .resolve(".gradle/sonar.properties")
  .takeIf { it.isFile }
  ?.let { file -> Properties().also { props -> file.inputStream().use { input -> props.load(input) } } }
val sonarTokenProvider = providers.environmentVariable("SONAR_TOKEN")
  .orElse(providers.gradleProperty("sonar.token"))
  .orElse(providers.provider { projectLocalSonarProps?.getProperty("sonar.token")?.takeIf { it.isNotBlank() } })
val sonarHostUrl = providers.environmentVariable("SONAR_HOST_URL")
  .orElse(providers.gradleProperty("sonar.host.url"))
  .getOrElse("http://localhost:9000")
val sonarProjectKey = providers.gradleProperty("sonar.projectKey")
  .getOrElse("link-aggregator")

sonar {
  properties {
    property("sonar.projectKey", sonarProjectKey)
    property("sonar.host.url", sonarHostUrl)
    sonarTokenProvider.orNull?.let { token -> property("sonar.token", token) }
    property("sonar.java.coveragePlugin", "jacoco")
    property("sonar.coverage.jacoco.xmlReportPaths", sonarJacocoXmlReport.get().asFile.absolutePath)
    property("sonar.java.binaries", binaries)
    property("sonar.sources", projectSources)
    property("sonar.java.test.binaries", testBinaries)
    property("sonar.projectBaseDir", rootProject.projectDir.absolutePath)
    property("sonar.exclusions", sonarExclusions)
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

val verifySonarJacocoReport = tasks.register("verifySonarJacocoReport") {
  group = "verification"
  description = "Verifies that the aggregated JaCoCo XML report consumed by SonarQube exists and is valid"
  dependsOn(tasks.testCodeCoverageReport)
  doLast {
    val reportFile = sonarJacocoXmlReport.get().asFile
    check(reportFile.exists()) {
      "JaCoCo aggregated report not found at ${reportFile.absolutePath}. " +
        "Run './gradlew testCodeCoverageReport' first."
    }
    val content = reportFile.readText()
    check(content.contains("<report")) {
      "JaCoCo aggregated report is not a valid report (missing <report>) at ${reportFile.absolutePath}."
    }
    val sourceFileCount = Regex("<sourcefile ").findAll(content).count()
    check(sourceFileCount > 0) {
      "JaCoCo aggregated report contains no <sourcefile> entries at ${reportFile.absolutePath}."
    }
    val instructionCounters = Regex("<counter type=\"INSTRUCTION\" missed=\"(\\d+)\" covered=\"(\\d+)\"/>")
      .findAll(content)
      .toList()
    check(instructionCounters.isNotEmpty()) {
      "JaCoCo aggregated report contains no INSTRUCTION counters at ${reportFile.absolutePath}."
    }
    val coveredInstructions = instructionCounters.last().groupValues[2].toLong()
    check(coveredInstructions > 0) {
      "JaCoCo aggregated report is an all-zero report (no covered instructions) at ${reportFile.absolutePath}."
    }
    logger.lifecycle(
      "\n=== SonarQube JaCoCo report verified ===\n" +
        "File: ${reportFile.absolutePath}\n" +
        "Source files: $sourceFileCount\n" +
        "Covered instructions: $coveredInstructions\n"
    )
  }
}

val sonarTokenRequiredMessage =
  "SONAR_TOKEN is required to run SonarQube analysis. Provide it via the SONAR_TOKEN " +
    "environment variable or the -Psonar.token=<token> Gradle property."

val failWithoutSonarToken: () -> Unit = {
  check(sonarTokenProvider.isPresent) { sonarTokenRequiredMessage }
}

gradle.taskGraph.whenReady {
  val sonarRequested = allTasks.any { task -> task.name == "sonar" || task.name == "sonarqube" }
  if (sonarRequested && !sonarTokenProvider.isPresent) {
    throw GradleException(sonarTokenRequiredMessage)
  }
}

tasks.named("sonar") {
  dependsOn(verifySonarJacocoReport)
  doFirst { failWithoutSonarToken() }
}

tasks.named("sonarqube") {
  dependsOn(verifySonarJacocoReport)
  doFirst { failWithoutSonarToken() }
}

val sonarAnalyzeEnabled = providers.environmentVariable("SONAR_ANALYZE")
  .orElse(providers.gradleProperty("sonar.analyze"))
  .map { it.toBoolean() }
  .getOrElse(false)

if (sonarAnalyzeEnabled) {
  tasks.named("check") { finalizedBy("sonar") }
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