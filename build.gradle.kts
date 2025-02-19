import space.kscience.gradle.useApache2Licence
import space.kscience.gradle.useSPCTeam

plugins {
    id("space.kscience.gradle.project")
}

val kmathVersion: String by extra("0.4.0")

allprojects {
    group = "space.kscience"
    version = "0.3.1-dev"

    repositories {
        mavenLocal()
        maven("https://repo.kotlin.link")
    }
}

ksciencePublish {
    pom("https://github.com/SciProgCentre/maps-kt") {
        useApache2Licence()
        useSPCTeam()
    }
    repository("spc","https://maven.sciprog.center/kscience")
    sonatype("https://oss.sonatype.org")
}

subprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://repo.kotlin.link")
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

readme.readmeTemplate = file("docs/templates/README-TEMPLATE.md")


