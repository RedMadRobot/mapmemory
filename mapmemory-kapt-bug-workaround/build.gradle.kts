plugins {
    id(libs.plugins.redmadrobot.kotlinLibrary)
    id(libs.plugins.redmadrobot.publish)
    kotlin("kapt")
}

description = "Workaround for bug in Kotlin compiler when use MapMemory with KAPT"

dependencies {
    api(projects.mapmemory)
    testRuntimeOnly(libs.junit.jupiter.engine)
}
