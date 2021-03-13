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
    "mapmemory-coroutines",
    "mapmemory-test"
)
