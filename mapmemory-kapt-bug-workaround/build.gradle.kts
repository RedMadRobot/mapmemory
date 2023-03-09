plugins {
    id(libs.plugins.redmadrobot.kotlinLibrary)
    id(libs.plugins.redmadrobot.publish)
    kotlin("kapt")
}

description = "Workaround for bug in Kotlin compiler when use MapMemory with KAPT"

dependencies {
    api(projects.mapmemory)
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.1")
}
