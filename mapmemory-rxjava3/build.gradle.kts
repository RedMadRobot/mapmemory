plugins {
    id("redmadrobot.kotlin-library")
    id("redmadrobot.publish")
}

description = "RxJava 3 accessors for MapMemory"

dependencies {
    api(project(":mapmemory"))
    api("io.reactivex.rxjava3:rxjava:3.0.11")
}
