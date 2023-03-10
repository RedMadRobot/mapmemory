enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs")
    }
}

rootProject.name = "mapmemory-root"

include(
    "mapmemory",
    "mapmemory-rxjava2",
    "mapmemory-rxjava3",
    "mapmemory-coroutines",
    "mapmemory-test",
    "mapmemory-kapt-bug-workaround"
)
