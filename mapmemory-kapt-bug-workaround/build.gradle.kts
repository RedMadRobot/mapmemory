plugins {
    id("redmadrobot.kotlin-library")
    id("redmadrobot.publish")
    kotlin("kapt")
}

description = "Workaround for bug in Kotlin compiler when use MapMemory with KAPT"

dependencies {
    api(project(":mapmemory"))
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.1")
}
