plugins {
    id("space.kscience.gradle.mpp")
    alias(spclibs.plugins.compose.compiler)
    alias(spclibs.plugins.compose.jb)
    `maven-publish`
}

kscience{
    jvm()
//    js()
    wasm()

    commonMain{
        api(projects.mapsKtFeatures)
    }
    jvmMain{
        implementation("org.jfree:org.jfree.svg:5.0.4")
        api(compose.desktop.currentOs)
    }
}


//java {
//    targetCompatibility = JVM_TARGET
//}