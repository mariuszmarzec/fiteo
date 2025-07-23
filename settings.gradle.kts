pluginManagement {
    repositories {
        mavenCentral()
        jcenter()
        maven { setUrl("https://plugins.gradle.org/m2/") }
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }

    versionCatalogs {
        create("kotlinWrappers") {
            val wrappersVersion = "2025.7.11"
            from("org.jetbrains.kotlin-wrappers:kotlin-wrappers-catalog:$wrappersVersion")
        }
    }
}
rootProject.name = "fiteo"
