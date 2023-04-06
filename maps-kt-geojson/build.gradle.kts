plugins {
    id("space.kscience.gradle.mpp")
    `maven-publish`
}


kscience{
    jvm()
    js()
    useSerialization {
        json()
    }
    dependencies{
        api(projects.mapsKtCore)
        api(projects.mapsKtFeatures)
        api(spclibs.kotlinx.serialization.json)
    }
}