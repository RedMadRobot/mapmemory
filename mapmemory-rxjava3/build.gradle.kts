plugins {
    id(libs.plugins.redmadrobot.kotlinLibrary)
    id(libs.plugins.redmadrobot.publish)
}

description = "RxJava 3 accessors for MapMemory"

dependencies {
    api(projects.mapmemory)
    api(libs.rxjava3)

    testImplementation(libs.bundles.test)
}
