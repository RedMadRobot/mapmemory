plugins {
    id(libs.plugins.redmadrobot.kotlinLibrary)
    id(libs.plugins.redmadrobot.publish)
}

description = "RxJava 2 accessors for MapMemory"

dependencies {
    api(projects.mapmemory)
    api(libs.rxjava2)

    testImplementation(libs.bundles.test)
}
