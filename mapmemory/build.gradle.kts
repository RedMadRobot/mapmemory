plugins {
    id(libs.plugins.redmadrobot.kotlinLibrary)
    id(libs.plugins.redmadrobot.publish)
}

description = "Simple in-memory cache conception built on Map"

dependencies {
    api(kotlin("stdlib", version = "1.5.21"))

    testImplementation(kotlin("test-junit5"))
    testImplementation("org.assertj:assertj-core:3.19.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.1")
}
