plugins {
    id(libs.plugins.redmadrobot.kotlinLibrary)
    id(libs.plugins.redmadrobot.publish)
}

description = "Coroutines accessors for MapMemory"

dependencies {
    api(projects.mapmemory)
    api(libs.kotlinx.coroutines)
}
