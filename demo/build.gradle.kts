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

dependencies {
    implementation(project(":lib"))
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.hamcrest:hamcrest-all:1.3")
}

tasks.test {
    useJUnitPlatform()
}