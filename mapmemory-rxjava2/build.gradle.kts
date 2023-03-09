plugins {
    id(libs.plugins.redmadrobot.kotlinLibrary)
    id(libs.plugins.redmadrobot.publish)
}

description = "RxJava 2 accessors for MapMemory"

dependencies {
    api(project(":mapmemory"))
    api("io.reactivex.rxjava2:rxjava:2.2.21")
}
