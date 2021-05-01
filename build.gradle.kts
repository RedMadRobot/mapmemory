import com.redmadrobot.build.extension.*

plugins {
    id("redmadrobot.root-project") version "0.8"
    id("com.github.ben-manes.versions") version "0.36.0"
    id("org.jetbrains.kotlinx.binary-compatibility-validator") version "0.5.0"
    `maven-publish`
}

apply(plugin = "redmadrobot.detekt")

redmadrobot {
    publishing {
        signArtifacts = !isRunningOnCi

        pom {
            setGitHubProject("RedMadRobot/mapmemory")

            licenses {
                mit()
            }

            developers {
                developer(id = "osipxd", name = "Osip Fatkullin", email = "o.fatkullin@redmadrobot.com")
            }
        }
    }
}

repositories {
    jcenter() // TODO: Remove when detekt-formatting plugin will be published to Maven Central
}

subprojects {
    group = "com.redmadrobot.mapmemory"
    version = "2.0-SNAPSHOT"

    apply(plugin = "maven-publish")

    publishing {
        repositories {
            if (isRunningOnCi) githubPackages("RedMadRobot/mapmemory")
            if (isReleaseVersion && credentialsExist("ossrh")) ossrh()
        }
    }
}

apiValidation {
    ignoredPackages.add("com.redmadrobot.mapmemory.internal")
}
