plugins {
    id("redmadrobot.kotlin-library")
    id("redmadrobot.publish")
}

description = "Utilities helping to test code that uses MapMemory"

dependencies {
    api(project(":mapmemory"))
}
