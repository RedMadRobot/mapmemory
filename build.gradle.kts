import com.redmadrobot.build.dsl.*

plugins {
    alias(libs.plugins.redmadrobot.publish.config)
    alias(libs.plugins.redmadrobot.detekt)
    alias(libs.plugins.versions)
    alias(libs.plugins.binaryCompatibilityValidator)
    `maven-publish`

    alias(libs.plugins.redmadrobot.kotlinLibrary) apply false
    alias(libs.plugins.kotlin) apply false
}

redmadrobot {
    jvmTarget.set(JavaVersion.VERSION_1_8)

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
    version = "2.1"

    apply(plugin = "maven-publish")
    apply(plugin = "io.gitlab.arturbosch.detekt")

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
