import com.redmadrobot.build.extension.credentialsExist
import com.redmadrobot.build.extension.githubPackages
import com.redmadrobot.build.extension.isSnapshotVersion
import com.redmadrobot.build.extension.rmrBintray

plugins {
    id("redmadrobot.root-project") version "0.6"
    `maven-publish`
}

apply(plugin = "redmadrobot.detekt")

subprojects {
    group = "com.redmadrobot.mapmemory"
    version = "1.1-SNAPSHOT"

    apply(plugin = "maven-publish")

    publishing {
        repositories {
            githubPackages("RedMadRobot/mapmemory")
            if (!isSnapshotVersion && credentialsExist("bintray")) rmrBintray(project.name)
        }
    }
}
