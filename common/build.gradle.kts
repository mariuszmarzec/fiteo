import com.codingfeline.buildkonfig.compiler.FieldSpec
import java.util.Properties
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack

buildscript {
    repositories {
        mavenCentral()
        jcenter()
    }

    dependencies {
        classpath("com.github.jengelman.gradle.plugins:shadow:5.2.0")
        classpath("mysql:mysql-connector-java:8.0.12")
        classpath("com.h2database:h2:${Dependency.h2_version}")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Dependency.kotlin_version}")
        classpath("org.jetbrains.kotlin:kotlin-serialization:${Dependency.kotlin_version}")
        classpath("com.codingfeline.buildkonfig:buildkonfig-gradle-plugin:${Dependency.buildkonfig_version}")
    }
}

plugins {
    kotlin("multiplatform") version Dependency.kotlin_version
    application
    id("org.flywaydb.flyway") version Dependency.flyway_version
    kotlin("plugin.serialization") version Dependency.kotlin_version
    id("com.codingfeline.buildkonfig") version Dependency.buildkonfig_version
}

application {
    mainClassName = "com.marzec.JvmMainKt"
}

val properties: Properties = Properties()
properties.load(project.rootProject.file("local.properties").inputStream())
val dbMigration = properties.getProperty("database.migration")
val dbEndpoint = properties.getProperty("database.endpoint")
val dbUser = properties.getProperty("database.user")
val dbPassword = properties.getProperty("database.password")
val dbDatabase = properties.getProperty("database.database")

val dbTestEndpoint = properties.getProperty("database.testEndpoint")
val dbTestUser = properties.getProperty("database.testUser")
val dbTestPassword = properties.getProperty("database.testPassword")
val dbTestDatabase = properties.getProperty("database.testDatabase")


val projectPackageName = "com.marzec.fiteo"


repositories {
    mavenCentral()
    jcenter()
    maven(url = "https://kotlin.bintray.com/kotlinx/")
    maven("https://kotlin.bintray.com/kotlin-js-wrappers/") // react, styled, ...
    maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/kotlin-js-wrappers") // react, styled, ...
}
group = projectPackageName
version = "1.0.0"

kotlin {

    jvm {
        withJava()
    }
    js {
        browser {
            commonWebpackConfig {
                cssSupport.enabled = true
            }
        }
        binaries.executable()
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                api("io.insert-koin:koin-core:${Dependency.koin_version}")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:${Dependency.datetime_version}")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:${Dependency.serialization_version}")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${Dependency.serialization_version}")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
                implementation("io.mockk:mockk-common:${Dependency.mockk_version}")
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(kotlin("stdlib-jdk8"))
                implementation("io.insert-koin:koin-ktor:${Dependency.koin_version}")
                implementation("io.ktor:ktor-html-builder:${Dependency.ktor_version}")
                implementation("io.ktor:ktor-server-sessions:${Dependency.ktor_version}")
                implementation("io.ktor:ktor-serialization:${Dependency.ktor_version}")
                implementation("io.ktor:ktor-auth:${Dependency.ktor_version}")
                implementation("io.ktor:ktor-server-netty:${Dependency.ktor_version}")
                implementation("org.slf4j:slf4j-simple:${Dependency.sl4j_version}")

                implementation("org.jetbrains.exposed:exposed-core:${Dependency.exposed_version}")
                implementation("org.jetbrains.exposed:exposed-dao:${Dependency.exposed_version}")
                implementation("org.jetbrains.exposed:exposed-java-time:${Dependency.exposed_version}")
                implementation("org.jetbrains.exposed:exposed-jdbc:${Dependency.exposed_version}")
                implementation("mysql:mysql-connector-java:${Dependency.mysql_connector_version}")
                implementation("com.h2database:h2:${Dependency.h2_version}")

            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
            }
        }
        val jsMain by getting {
            dependencies {
                implementation(kotlin("stdlib-js"))
                implementation("io.ktor:ktor-client-js:${Dependency.ktor_version}") //include http&websockets

                //ktor client js json
                implementation("io.ktor:ktor-client-json-js:${Dependency.ktor_version}")
                implementation("io.ktor:ktor-client-serialization-js:${Dependency.ktor_version}")

                implementation("org.jetbrains:kotlin-react:17.0.1-pre.148-kotlin-1.4.21")
                implementation("org.jetbrains:kotlin-react-dom:17.0.1-pre.148-kotlin-1.4.21")
                implementation(npm("react", "17.0.1"))
                implementation(npm("react-dom", "17.0.1"))

                implementation("org.jetbrains:kotlin-styled:5.2.1-pre.148-kotlin-1.4.21")
                implementation(npm("styled-components", "~5.2.1"))
            }
        }
    }
}

tasks.getByName<Jar>("jvmJar") {
    doFirst {
        manifest {
            attributes["Main-Class"] = "com.marzec.JvmMainKt"
        }
        from(configurations.getByName("runtimeClasspath").map { if (it.isDirectory) it else zipTree(it) })
    }
    val taskName = if (project.hasProperty("isProduction")) {
        "jsBrowserProductionWebpack"
    } else {
        "jsBrowserDevelopmentWebpack"
    }
    val webpackTask = tasks.getByName<KotlinWebpack>(taskName)
    dependsOn(webpackTask) // make sure JS gets compiled first
    from(File(webpackTask.destinationDirectory, webpackTask.outputFileName)) // bring output file along into the JAR
}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }
}

distributions {
    main {
        contents {
            from("$buildDir/libs") {
                rename("${rootProject.name}-jvm", rootProject.name)
                into("lib")
            }
        }
    }
}

flyway {
    if (dbMigration == "test") {
        url = dbTestEndpoint
        user = dbTestUser
        password = dbTestPassword
        createSchemas = true
        placeholders = mapOf("database_name" to dbTestDatabase)
    } else {
        cleanDisabled = true
        url = dbEndpoint
        user = dbUser
        password = dbPassword
        createSchemas = true
        placeholders = mapOf("database_name" to dbDatabase)
    }
    locations = arrayOf("filesystem:/$projectDir/src/commonMain/resources/db/migration")
}

buildkonfig {
    packageName = projectPackageName
    defaultConfigs {
        buildConfigField(FieldSpec.Type.STRING, "DB_ENDPOINT", dbEndpoint)
        buildConfigField(FieldSpec.Type.STRING, "DB_USER", dbUser)
        buildConfigField(FieldSpec.Type.STRING, "DB_PASSWORD", dbPassword)
        buildConfigField(FieldSpec.Type.STRING, "DB_DATABASE", dbDatabase)

        buildConfigField(FieldSpec.Type.STRING, "DB_TEST_ENDPOINT", dbTestEndpoint)
        buildConfigField(FieldSpec.Type.STRING, "DB_TEST_USER", dbTestUser)
        buildConfigField(FieldSpec.Type.STRING, "DB_TEST_PASSWORD", dbTestPassword)
        buildConfigField(FieldSpec.Type.STRING, "DB_TEST_DATABASE", dbTestDatabase)
    }
}

tasks.getByName<JavaExec>("run") {
    classpath(tasks.getByName<Jar>("jvmJar")) // so that the JS artifacts generated by `jvmJar` can be found and served
}