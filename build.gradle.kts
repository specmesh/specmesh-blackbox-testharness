/*
 * Copyright 2023 SpecMesh Contributors (https://github.com/specmesh)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

plugins {
    `java-library`
    signing
    `maven-publish`
    id("com.github.spotbugs") version "5.1.3"
    id("com.diffplug.spotless") version "6.22.0"
    id("pl.allegro.tech.build.axion-release") version "1.17.0"
    id("io.github.gradle-nexus.publish-plugin") version "1.3.0"

}
group = "io.specmesh"
project.version = scmVersion.version
version = scmVersion.version

allprojects {
    apply(plugin = "idea")
    apply(plugin = "java")
    apply(plugin = "checkstyle")
    apply(plugin = "com.diffplug.spotless")
    apply(plugin = "com.github.spotbugs")

    tasks.jar {
        onlyIf { sourceSets.main.get().allSource.files.isNotEmpty() }
    }
}

subprojects {
    project.version = project.parent?.version!!

    apply(plugin = "maven-publish")
    apply(plugin = "signing")
    repositories {
        mavenCentral()
        maven {
            url = uri("https://packages.confluent.io/maven/")
            group = "io.confluent"
        }

    }
    configurations.all {
        resolutionStrategy.eachDependency {
            if (requested.group == "org.slf4j" && requested.name == "slf4j-api") {
                useVersion("2.0.9")
            }
            if (requested.group == "org.apache.avro" && requested.name == "avro") {
                useVersion("1.11.3")
            }
        }
        exclude(group = "ch.qos.logback", module = "logback-classic")
    }


    tasks.test {
        // Use junit platform for unit tests.
        useJUnitPlatform()
    }


    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(11))
        }
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11

        withSourcesJar()
        withJavadocJar()
    }

    extra.apply {
        set("specmeshVersion", "0.9.2")
        set("specmeshDataGenVersion", "0.5.3")
        set("kafkaVersion", "7.5.3-ce")
        set("confluentVersion", "7.5.3")
        set("testcontainersVersion", "1.18.3")
        set("openTracingVersion", "0.33.0")
        set("observabilityVersion", "1.1.8")
        set("guavaVersion", "32.1.2-jre")
        set("jacksonVersion", "2.15.2")
        set("protobufVersion", "3.24.3")
        set("medeiaValidatorVersion", "1.1.0")
        set("junitVersion", "5.10.0")
        set("mockitoVersion", "5.5.0")
        set("junitPioneerVersion", "2.1.0")
        set("spotBugsVersion", "4.7.3")
        set("hamcrestVersion", "1.3")
        set(
            "log4jVersion",
            "2.22.1"
        )           // https://mvnrepository.com/artifact/org.apache.logging.log4j/log4j-core
        set("classGraphVersion", "4.8.21")
        set("testcontainersVersion", "1.18.3")
        set("lombokVersion", "1.18.30")
    }

    val specmeshVersion: String by extra
    val specmeshDataGenVersion: String by extra
    val junitVersion: String by extra
    val jacksonVersion: String by extra
    val mockitoVersion: String by extra
    val junitPioneerVersion: String by extra
    val guavaVersion: String by extra
    val hamcrestVersion: String by extra
    val log4jVersion: String by extra
    val testcontainersVersion: String by extra
    val confluentVersion: String by extra
    val kafkaVersion: String by extra

    configurations.all {
        resolutionStrategy.eachDependency {
            if (requested.group == "org.slf4j" && requested.name == "slf4j-api") {
                useVersion("2.0.9")
            }
        }
    }
    dependencies {

        implementation("io.specmesh:specmesh-cli:$specmeshVersion")
        implementation("io.specmesh:specmesh-kafka:$specmeshVersion")
        implementation("io.specmesh:kafka-random-generator:$specmeshDataGenVersion")

        implementation("io.confluent:kafka-schema-registry-client:$confluentVersion")
        implementation("io.confluent:kafka-json-schema-provider:$confluentVersion")
        implementation("io.confluent:kafka-avro-serializer:$confluentVersion")
        implementation("io.confluent:kafka-json-schema-serializer:$confluentVersion")
        implementation("io.confluent:kafka-protobuf-provider:$confluentVersion")
        implementation("io.confluent:kafka-protobuf-serializer:$confluentVersion")
        implementation("io.confluent:kafka-streams-protobuf-serde:$confluentVersion")
        implementation("io.confluent:kafka-streams-avro-serde:$confluentVersion")
        implementation("com.google.protobuf:protobuf-java:3.25.3")

        implementation("org.apache.kafka:kafka-streams:$kafkaVersion")
        implementation("org.apache.kafka:kafka-clients:$kafkaVersion")
        implementation("commons-io:commons-io:2.14.0")
        implementation("com.fasterxml.jackson.core:jackson-annotations:$jacksonVersion")
        implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:$jacksonVersion")

        implementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
        implementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
        implementation("org.testcontainers:testcontainers:$testcontainersVersion") {
            exclude(group = "org.slf4j", module = "slf4j-api")
        }
        implementation("org.testcontainers:kafka:$testcontainersVersion")


        implementation("org.apache.commons:commons-math3:3.6.1")

        implementation("com.google.guava:guava:33.0.0-jre")


        testImplementation("org.hamcrest:hamcrest-all:1.3")

        testImplementation("org.junit-pioneer:junit-pioneer:$junitPioneerVersion")
        testImplementation("org.mockito:mockito-junit-jupiter:$mockitoVersion")
        testImplementation("org.hamcrest:hamcrest-all:$hamcrestVersion")
        testImplementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:$jacksonVersion")
        testImplementation("com.google.guava:guava-testlib:$guavaVersion")
        //    testImplementation("org.apache.logging.log4j:log4j-core:$log4jVersion")
        //    testImplementation("org.apache.logging.log4j:log4j-slf4j2-impl:$log4jVersion")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")

    }

    tasks.compileJava {
        options.compilerArgs.add("-Xlint:all,-serial,-processing")
    }

    tasks.test {
        useJUnitPlatform()
        setForkEvery(1)
        maxParallelForks = 2
        testLogging {
            showStandardStreams = true
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
            showCauses = true
            showExceptions = true
            showStackTraces = true
        }
    }

    tasks.javadoc {
        if (JavaVersion.current().isJava9Compatible) {
            (options as StandardJavadocDocletOptions).apply {
                addBooleanOption("html5", true)
                // Why -quite? See: https://github.com/gradle/gradle/issues/2354
                addStringOption("Xwerror", "-quiet")
            }
        }
    }

    spotless {
        java {
            googleJavaFormat("1.15.0").aosp().reflowLongStrings()
            indentWithSpaces()
            importOrder()
            removeUnusedImports()
            trimTrailingWhitespace()
            endWithNewline()
            targetExclude("**/build/generated/source*/**/*.*")
        }
    }

    tasks.register("format") {
        dependsOn("spotlessCheck", "spotlessApply")
    }

    tasks.register("checkstyle") {
        dependsOn("checkstyleMain", "checkstyleTest")
    }

    tasks.register("spotbugs") {
        dependsOn("spotbugsMain", "spotbugsTest")
    }

    tasks.register("static") {
        dependsOn("checkstyle", "spotbugs")
    }

    spotbugs {
        excludeFilter.set(rootProject.file("config/spotbugs/suppressions.xml"))

        tasks.spotbugsMain {
            reports.create("html") {
                required.set(true)
                setStylesheet("fancy-hist.xsl")
            }
        }
        tasks.spotbugsTest {
            reports.create("html") {
                required.set(true)
                setStylesheet("fancy-hist.xsl")
            }
        }
    }

    tasks.jar {
        archiveBaseName.set("specmesh-${project.name}")
    }

    publishing {
        repositories {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/specmesh/${rootProject.name}")
                credentials {
                    username = System.getenv("GITHUB_ACTOR")
                    password = System.getenv("GITHUB_TOKEN")
                }
            }
        }

        publications {
            create<MavenPublication>("mavenArtifacts") {
                from(components["java"])

                artifactId = "specmesh-${artifactId}"
                project.group = "io.specmesh"

                pom {
                    name.set("${project.group}:${artifactId}")

                    description.set("Blackbox system test runner")

                    url.set("https://github.com/specmesh/specmesh-blackbox-testharness/")

                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }

                    developers {
                        developer {
                            name.set("Neil Avery")
                            email.set("8012398+neil-avery@users.noreply.github.com")
                            organization.set("SpecMesh Master Builders")
                            organizationUrl.set("https://www.specmesh.io")
                        }

                        developer {
                            name.set("Andy Coates")
                            email.set("8012398+big-andy-coates@users.noreply.github.com")
                            organization.set("SpecMesh Master Builders")
                            organizationUrl.set("https://www.specmesh.io")
                        }
                    }

                    scm {
                        connection.set("scm:git:git://github.com/specmesh/${rootProject.name}.git")
                        developerConnection.set("scm:git:ssh://github.com/specmesh/${rootProject.name}.git")
                        url.set("https://github.com/specmesh/${rootProject.name}")
                    }

                    issueManagement {
                        url.set("https://github.com/specmesh/${rootProject.name}/issues")
                    }
                }
            }
        }
    }

    signing {
        setRequired {
            !project.version.toString().endsWith("-SNAPSHOT")
                    && !project.hasProperty("skipSigning")
        }

        if (project.hasProperty("signingKey")) {
            useInMemoryPgpKeys(properties["signingKey"].toString(), properties["signingPassword"].toString())
        }

        sign(publishing.publications["mavenArtifacts"])
    }



}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))

            if (project.hasProperty("SONA_USERNAME")) {
                username.set(project.property("SONA_USERNAME").toString())
            }

            if (project.hasProperty("SONA_PASSWORD")) {
                password.set(project.property("SONA_PASSWORD").toString())
            }
        }
    }
}

//release.dependsOn( javadoc, sourceJar)


//artifacts {
//    archivesjar, javadocJar, sourceJar
//}

