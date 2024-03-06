plugins {
    id("java")
}

group = "org.example"
version = "0.0.1"

val lombokVersion = "1.18.30"


dependencies {
    implementation(project(":lib"))
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.hamcrest:hamcrest-all:1.3")
    compileOnly("org.projectlombok:lombok:$lombokVersion")
    annotationProcessor("org.projectlombok:lombok:$lombokVersion")
    testCompileOnly("org.projectlombok:lombok:$lombokVersion")
    testAnnotationProcessor("org.projectlombok:lombok:$lombokVersion")

}

tasks.test {
    useJUnitPlatform()
}