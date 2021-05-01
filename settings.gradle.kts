pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
    }

    resolutionStrategy {
        eachPlugin {
            if (requested.id.namespace == "redmadrobot") {
                useModule("com.redmadrobot.build:infrastructure:${requested.version}")
            }
        }
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
