plugins {
    id("com.redmadrobot.kotlin-library")
    id("com.redmadrobot.publish")
}

description = "Utilities helping to test code that uses MapMemory"

dependencies {
    api(project(":mapmemory"))

    testImplementation(kotlin("test-junit5"))
    testImplementation("org.assertj:assertj-core:3.19.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.1")
}
