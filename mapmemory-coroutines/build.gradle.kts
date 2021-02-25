plugins {
    id("redmadrobot.kotlin-library")
    id("redmadrobot.publish")
}

description = "Coroutines accessors for MapMemory"

dependencies {
    api(project(":mapmemory"))
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")
}
