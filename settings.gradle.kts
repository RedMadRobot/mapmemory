pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
    }
}

rootProject.name = "mapmemory"

include(
    "mapmemory",
    "mapmemory-rxjava2",
    "mapmemory-rxjava3",
    "mapmemory-coroutines",
    "mapmemory-test",
    "mapmemory-kapt-bug-workaround"
)
