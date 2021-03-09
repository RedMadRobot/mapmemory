plugins {
    id("redmadrobot.kotlin-library")
    id("redmadrobot.publish")
}

description = "Simple in-memory cache conception built on Map"

dependencies {
    testImplementation("org.assertj:assertj-core:3.19.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.1")
}
