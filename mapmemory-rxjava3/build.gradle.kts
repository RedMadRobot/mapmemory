plugins {
    id(libs.plugins.redmadrobot.kotlinLibrary)
    id(libs.plugins.redmadrobot.publish)
}

description = "RxJava 3 accessors for MapMemory"

dependencies {
    api(project(":mapmemory"))
    api("io.reactivex.rxjava3:rxjava:3.0.11")
}
