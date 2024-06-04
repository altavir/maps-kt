plugins {
    id("space.kscience.gradle.mpp")
    alias(spclibs.plugins.compose.compiler)
    alias(spclibs.plugins.compose.jb)
    `maven-publish`
}

val kmathVersion: String by rootProject.extra

kscience {
    jvm()
//    js()
    wasm{
        browser {
            testTask {
                enabled = false
            }
        }
    }

    useCoroutines()

    useSerialization {
        json()
    }

    useSerialization(sourceSet = space.kscience.gradle.DependencySourceSet.TEST) {
        protobuf()
    }

    commonMain{
        api(projects.trajectoryKt)
        api(compose.runtime)
        api(compose.foundation)
        api(compose.material)
        api(compose.ui)
        api("io.github.oshai:kotlin-logging:6.0.3")
    }
}