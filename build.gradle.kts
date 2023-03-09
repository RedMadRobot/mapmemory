import com.redmadrobot.build.dsl.*

plugins {
    id("com.redmadrobot.publish-config") version "0.18"
    id("com.redmadrobot.detekt") version "0.18"
    id("com.github.ben-manes.versions") version "0.36.0"
    id("org.jetbrains.kotlinx.binary-compatibility-validator") version "0.5.0"
    `maven-publish`

    id("com.redmadrobot.kotlin-library") version "0.18" apply false
    kotlin("jvm") version "1.8.10" apply false
}

redmadrobot {
    publishing {
        signArtifacts.set(!isRunningOnCi)

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
    mavenCentral()
}

subprojects {
    group = "com.redmadrobot.mapmemory"
    version = "2.1-SNAPSHOT"

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
