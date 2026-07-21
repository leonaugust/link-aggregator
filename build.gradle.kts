@file:Suppress("UNCHECKED_CAST")

import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension


allprojects {
    apply(plugin = "java-library")

    repositories {
        mavenCentral()
    }
}

subprojects {
    dependencies {
        implementation(rootProject.libs.slf4j)

        compileOnly(rootProject.libs.lombok)
        testCompileOnly(rootProject.libs.lombok)
        annotationProcessor(rootProject.libs.lombok)
        testAnnotationProcessor(rootProject.libs.lombok)

        implementation(rootProject.libs.mapstruct)
        annotationProcessor(rootProject.libs.mapstruct.processor)

        implementation(rootProject.libs.jackson)

        testImplementation(platform(rootProject.libs.junit.bom))
    }

    if (!project.path.startsWith(":core")) {
        apply(plugin = "io.spring.dependency-management")
        the<DependencyManagementExtension>().apply {
            imports {
                mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
            }
        }
        dependencies {
            implementation("org.springframework.boot:spring-boot-starter-log4j2")
            testImplementation("org.springframework.boot:spring-boot-starter-log4j2")
            testImplementation("org.springframework.boot:spring-boot-starter-test")

            implementation(rootProject.libs.mapstruct.spring)
            annotationProcessor(rootProject.libs.mapstruct.spring)
            annotationProcessor(rootProject.libs.mapstruct)
        }
    }
}

plugins {
    id("java-library")
    id("maven-publish")
    id("jacoco")
    alias(libs.plugins.spring.boot) apply false
}

jacoco {
    toolVersion = "0.8.13"
    reportsDirectory.set(layout.buildDirectory.dir("jacocoReports"))
}

val jacocoCli = configurations.create("jacocoCli")
dependencies {
    jacocoCli("org.jacoco:org.jacoco.cli:${jacoco.toolVersion}:nodeps") {
        isTransitive = false
    }
}

val jacocoExcludePatterns = listOf(
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
)

val subProjectsWithoutGrouping = subprojects - project(":adapters") - project(":core")
configure(subProjectsWithoutGrouping) {

    apply(plugin = "java-library")
    apply(plugin = "maven-publish")
    apply(plugin = "jacoco")

    group = "ru.leonaugust.link_aggregator"

    ext["jacocoExclude"] = jacocoExcludePatterns

    configurations.all {
        exclude(group = "org.springdoc", module = "springdoc-openapi-starter-webflux-api")
        exclude(group = "org.springdoc", module = "springdoc-openapi-starter-webflux-ui")
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
        exclude(group = "ch.qos.logback", module = "logback-classic")
        exclude(group = "ch.qos.logback", module = "logback-core")

        exclude(group = "net.jpountz.lz4", module = "lz4")
    }

    jacoco {
        toolVersion = rootProject.jacoco.toolVersion
        reportsDirectory.set(layout.buildDirectory.dir("jacocoReports"))
    }

//    tasks.jacocoTestReport {
//        enabled = false
//    }

    java {

        val javaVersion = JavaVersion.VERSION_17

        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion

        withJavadocJar()
        withSourcesJar()
    }

    tasks.compileJava {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-parameters")

        val isMapstructContains = configurations
            .asSequence()
            .filter { it.name in listOf("annotationProcessor", "testAnnotationProcessor") }
            .flatMap { it.dependencies }
            .any { it.group == "org.mapstruct" }

        if (isMapstructContains) {
            options.compilerArgs.add("-Amapstruct.unmappedTargetPolicy=ERROR")
        }
    }

    tasks.compileTestJava {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-parameters")
    }

    tasks.javadoc {
        onlyIf {
            source.asFileTree
                .any { it.name.endsWith(".java") && !it.name.endsWith("package-info.java") }
        }
        options.encoding = "UTF-8"
        (this.options as CoreJavadocOptions).addStringOption("Xdoclint:none", "-quiet")
    }

    tasks.check {
        dependsOn("integrationTest")
        val allDependedProjectCheckTasks = configurations.flatMap { configuration ->
            configuration.dependencies
                .filterIsInstance<ProjectDependency>()
                .map { project.project(it.path).tasks.check }
        }
        shouldRunAfter(allDependedProjectCheckTasks)
    }

    tasks.test {
        failFast = true

        setIncludes(listOf("**/*UTest.*"))
        filter {
            isFailOnNoMatchingTests = false
        }

        jvmArgs = listOf("-Duser.timezone=UTC -Dfile.encoding=UTF-8")
        useJUnitPlatform()
        configure<JacocoTaskExtension> {
            excludes = jacocoExcludePatterns
            setDestinationFile(layout.buildDirectory.file("jacoco/${project.name}-UTest.exec").get().asFile)
        }
    }

    tasks.register<Test>("integrationTest") {
        val testSourceSet = project.sourceSets["test"]
        testClassesDirs = testSourceSet.output.classesDirs
        classpath = testSourceSet.runtimeClasspath

        failFast = true

//        setIncludes(listOf("**/*IT.*"))
        filter {
            isFailOnNoMatchingTests = false
        }
        shouldRunAfter("test")

        jvmArgs = listOf("-Duser.timezone=UTC -Dfile.encoding=UTF-8 -Ddebug=true")
        useJUnitPlatform()
//        testLogging {
//            showStandardStreams = true // чтобы gradle показывал логи
//        }
        doFirst {
            delete(file("build/test-logs"))
        }
        doLast {
            val logFile = layout.buildDirectory
                .file("test-logs/integration-tests.log")
                .get().asFile
            val logUri = logFile.toURI().toString()
            logger.lifecycle(
                "\n=== Integration test log file ===\n" +
                        "LOG: $logUri\n"
            )
        }
        configure<JacocoTaskExtension> {
            excludes = jacocoExcludePatterns
            setDestinationFile(layout.buildDirectory.file("jacoco/${project.name}-ITest.exec").get().asFile)
        }
    }

    tasks.withType<PublishToMavenRepository>().configureEach {
        val mavenProjectCode = project.properties["mavenProjectCode"]!!
        val predicate = provider {
            (repository == publishing.repositories["$mavenProjectCode-maven-snapshot"] && version.toString().endsWith("SNAPSHOT"))
                    || (repository != publishing.repositories["$mavenProjectCode-maven-snapshot"] && !version.toString()
                .endsWith("SNAPSHOT"))
        }
        onlyIf {
            predicate.get()
        }
    }
}

val allSubProjectsCheckTasks = subProjectsWithoutGrouping
    .map {
        it.tasks.check
    }

val pathToMergedExecFile = layout.buildDirectory.file("jacoco/aggregated.exec").get().asFile.absolutePath
val jacocoMerge = tasks.register<JavaExec>("jacocoMerge") {
    group = "leon_august"
    description = "Merges multiple JaCoCo .exec files"

    logger.lifecycle("JaCoCo ALL required tasks: ${allSubProjectsCheckTasks.map { it.get().project.name + ":check" }}")
    mustRunAfter(allSubProjectsCheckTasks)
    delete(pathToMergedExecFile)

    classpath = configurations.named("jacocoCli").get()
    outputs.file(pathToMergedExecFile)

    doFirst {
        val execFileCollection = fileTree(project.rootDir.absolutePath) {
            include("**/build/jacoco/*.exec")
        }
            .files
            .map { it.absolutePath }
            .filter { !it.equals(pathToMergedExecFile) }

        logger.lifecycle("JaCoCo following files will be merged in a single one: $execFileCollection")

        //это важно, чтобы КАЖДЫЙ exec-файл был добавлен как отдельный аргумент
        args = listOf("merge") + execFileCollection + listOf("--destfile", pathToMergedExecFile)
    }

    doLast {
        logger.lifecycle("JaCoCo merged all exec-files into the aggregated one: $pathToMergedExecFile")
    }
}

val jacocoRootReport = tasks.register<JacocoReport>("jacocoRootReport") {

    group = "leon_august"
    description = "Create a full jacoco report based on all run tests"

    dependsOn(jacocoMerge)

    sourceDirectories.setFrom(files(subProjectsWithoutGrouping.map { it.the<SourceSetContainer>()["main"].allSource.srcDirs }))
    classDirectories.setFrom(
        files(
            subProjectsWithoutGrouping
                .map {
                    it.the<SourceSetContainer>()["main"].output
                }
        )
            .map {
                fileTree(it) {
                    exclude(jacocoExcludePatterns)
                }
            }
    )
    executionData.setFrom(jacocoMerge.get().outputs)

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
}

val jacocoRootCoverageVerification = tasks.register<JacocoCoverageVerification>("jacocoRootCoverageVerification") {

    enabled = false
    group = "leon_august"
    description = "Run coverage verification"

    dependsOn(jacocoRootReport)

    executionData.setFrom(jacocoRootReport.get().executionData)
    classDirectories.setFrom(jacocoRootReport.get().classDirectories)
    sourceDirectories.setFrom(jacocoRootReport.get().sourceDirectories)

    var totalLineCoverage = (project.properties["totalLineCoverage"] as? String)?.toBigDecimal() ?: BigDecimal.ZERO
    var totalBranchCoverage = (project.properties["totalBranchCoverage"] as? String)?.toBigDecimal() ?: BigDecimal.ZERO
    var methodComplexityCoverage = (project.properties["methodComplexityCoverage"] as? String)?.toBigDecimal() ?: BigDecimal.ZERO
    var allowUntestedClasses = (project.properties["allowUntestedClasses"] as? String)?.toBoolean() ?: true

    violationRules {
        rule {
            limit {
                counter = "INSTRUCTION"
                value = "COVEREDRATIO"
                minimum = totalLineCoverage // % overall
            }
            limit {
                counter = "BRANCH"
                value = "COVEREDRATIO"
                minimum = totalBranchCoverage // % branch coverage
            }
        }

        if (methodComplexityCoverage != BigDecimal.ZERO) {
            rule {
                element = "METHOD"
                limit {
                    counter = "COMPLEXITY"
                    value = "COVEREDRATIO"
                    minimum = methodComplexityCoverage // % of paths were tested
                }
            }
        }

        if (!allowUntestedClasses) {
            rule {
                element = "CLASS"
                limit {
                    counter = "CLASS"
                    value = "MISSEDCOUNT"
                    maximum = "0".toBigDecimal()
                }
            }
        }
    }
}

tasks.check {
    finalizedBy(jacocoRootCoverageVerification)
    shouldRunAfter(allSubProjectsCheckTasks)
}

tasks.named<Jar>("jar") {
    enabled = false
}

tasks.test {
    enabled = false
}

var modulesForSonar = subProjectsWithoutGrouping
val binaries = modulesForSonar.joinToString(",") { it.projectDir.absolutePath.plus("/build/classes/java/main") }
val testBinaries = modulesForSonar.joinToString(",") { it.projectDir.absolutePath.plus("/build/classes/java/test") }
val projectSources = modulesForSonar.joinToString(",") { it.projectDir.absolutePath.plus("/src/main/java/") }