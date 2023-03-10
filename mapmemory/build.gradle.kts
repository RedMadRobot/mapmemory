plugins {
    id(libs.plugins.redmadrobot.kotlinLibrary)
    id(libs.plugins.redmadrobot.publish)
}

description = "Simple in-memory cache conception built on Map"

dependencies {
    api(kotlin("stdlib", version = "1.5.21"))

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.test)
}
