pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        maven(url = "https://dl.bintray.com/redmadrobot-opensource/android")
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
