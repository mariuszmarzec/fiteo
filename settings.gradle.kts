pluginManagement {
    repositories {
        mavenCentral()
        jcenter()
        maven { setUrl("https://dl.bintray.com/kotlin/kotlin-eap") }
        maven { setUrl("https://plugins.gradle.org/m2/") }
    }
}

rootProject.name = "fiteo"

enableFeaturePreview("GRADLE_METADATA")
include(":common")