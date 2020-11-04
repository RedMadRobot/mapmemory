plugins {
    val infrastructureVersion = "0.3"
    id("redmadrobot.kotlin-library") version infrastructureVersion apply false
    id("redmadrobot.publish") version infrastructureVersion apply false
    id("redmadrobot.detekt") version infrastructureVersion
}

subprojects {
    group = "com.redmadrobot.mapmemory"
    version = "1.0"
}
