plugins {
    id("java")
}

group = "org.example"
version = "0.0.1"

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
    }
    exclude(group = "ch.qos.logback", module = "logback-classic")
}

dependencies {
    implementation(project(":lib"))
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.hamcrest:hamcrest-all:1.3")
}

tasks.test {
    useJUnitPlatform()
}