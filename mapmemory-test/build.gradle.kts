plugins {
    id(libs.plugins.redmadrobot.kotlinLibrary)
    id(libs.plugins.redmadrobot.publish)
}

description = "Utilities helping to test code that uses MapMemory"

dependencies {
    api(projects.mapmemory)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.test)
}
