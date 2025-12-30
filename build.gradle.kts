import com.codingfeline.buildkonfig.compiler.FieldSpec
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import java.util.Properties

buildscript {
    repositories {
        jcenter()
        gradlePluginPortal()
        mavenCentral()
    }

    dependencies {
        classpath("org.flywaydb:flyway-mysql:${Dependency.flyway_version}")
        classpath("mysql:mysql-connector-java:${Dependency.mysql_connector_version}")
        classpath("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:${Dependency.detekt_version}")
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
    id("io.gitlab.arturbosch.detekt") version Dependency.detekt_version
    jacoco
}

application {
    mainClass.set("com.marzec.JvmMainKt")
}

val configurationProperties: Properties = Properties()
configurationProperties.load(project.rootProject.file("local.properties").inputStream())
val dbMigration = configurationProperties.getProperty("database.migration")
val dbEndpoint = configurationProperties.getProperty("database.endpoint")
val dbUser = configurationProperties.getProperty("database.user")
val dbPassword = configurationProperties.getProperty("database.password")
val dbDatabase = configurationProperties.getProperty("database.database")

val dbTestEndpoint = configurationProperties.getProperty("database.testEndpoint")
val dbTestUser = configurationProperties.getProperty("database.testUser")
val dbTestPassword = configurationProperties.getProperty("database.testPassword")
val dbTestDatabase = configurationProperties.getProperty("database.testDatabase")

val firebaseServiceAccountProd = configurationProperties.getProperty("firebaseServiceAccount.prod")
val firebaseServiceAccountTest = configurationProperties.getProperty("firebaseServiceAccount.test")

val projectPackageName = "com.marzec.fiteo"

repositories {
    jcenter()
    maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/kotlin-js-wrappers") // react, styled, ...
    maven("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven") // react, styled, ...
    mavenCentral()
}
group = projectPackageName
version = "1.0.0"

kotlin {

    jvm {
        compilerOptions {
            jvmTarget = JvmTarget.fromTarget("17")
        }

        tasks.test {
            useJUnitPlatform()
            testLogging {
                events("passed", "skipped", "failed")
            }
        }
    }
    js {
        browser {
            commonWebpackConfig {
                cssSupport {
                    enabled.set(true)
                }
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
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(kotlin("stdlib-jdk8"))
                implementation("io.insert-koin:koin-ktor:${Dependency.koin_ktor_version}")
                implementation("io.insert-koin:koin-logger-slf4j:${Dependency.koin_version}")
                implementation("io.ktor:ktor-server-html-builder:${Dependency.ktor_version}")
                implementation("io.ktor:ktor-server-sessions:${Dependency.ktor_version}")
                implementation("io.ktor:ktor-server-cors:${Dependency.ktor_version}")
                implementation("io.ktor:ktor-server-call-logging:${Dependency.ktor_version}")
                implementation("io.ktor:ktor-server-compression:${Dependency.ktor_version}")
                implementation("io.ktor:ktor-server-content-negotiation:${Dependency.ktor_version}")
                implementation("io.ktor:ktor-server-default-headers:${Dependency.ktor_version}")
                implementation("io.ktor:ktor-serialization-kotlinx-json:${Dependency.ktor_version}")
                implementation("io.ktor:ktor-server-content-negotiation:${Dependency.ktor_version}")
                implementation("io.ktor:ktor-server-auth:${Dependency.ktor_version}")
                implementation("io.ktor:ktor-server-auth-jwt:${Dependency.ktor_version}")
                implementation("io.ktor:ktor-server-netty:${Dependency.ktor_version}")
                implementation("io.ktor:ktor-server-sse-jvm:${Dependency.ktor_version}")
                implementation("org.slf4j:slf4j-simple:${Dependency.sl4j_version}")
                implementation("ch.qos.logback:logback-classic:${Dependency.logback_version}")

                implementation("org.jetbrains.exposed:exposed-core:${Dependency.exposed_version}")
                implementation("org.jetbrains.exposed:exposed-dao:${Dependency.exposed_version}")
                implementation("org.jetbrains.exposed:exposed-java-time:${Dependency.exposed_version}")
                implementation("org.jetbrains.exposed:exposed-jdbc:${Dependency.exposed_version}")
                implementation("mysql:mysql-connector-java:${Dependency.mysql_connector_version}")

                implementation("io.mockk:mockk:${Dependency.mockk_version}")
                implementation("com.google.firebase:firebase-admin:${Dependency.firebase_admin_version}")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-junit"))
                implementation("io.insert-koin:koin-test:${Dependency.koin_version}")
                implementation("io.insert-koin:koin-test-junit4:${Dependency.koin_version}")
//                implementation("io.ktor:ktor-server-tests:${Dependency.ktor_version}")
                implementation("io.ktor:ktor-server-test-host:${Dependency.ktor_version}")
                implementation("io.ktor:ktor-server-netty:${Dependency.ktor_version}")

                implementation("org.flywaydb:flyway-core:${Dependency.flyway_version}")
                implementation("org.flywaydb:flyway-mysql:${Dependency.flyway_version}")

                implementation("com.google.truth:truth:${Dependency.truth_version}")
                implementation("com.google.truth.extensions:truth-java8-extension:${Dependency.truth_version}")
            }
        }
        val jsMain by getting {
            dependencies {
                implementation(kotlin("stdlib-js"))
                implementation("io.ktor:ktor-client-content-negotiation:${Dependency.ktor_version}")
                implementation("io.ktor:ktor-serialization-kotlinx-json:${Dependency.ktor_version}")
                implementation("io.ktor:ktor-client-js:${Dependency.ktor_version}") //include http&websockets
                implementation(npm("text-encoding", "0.7.0"))
                //ktor client js json
                implementation("io.ktor:ktor-client-json-js:${Dependency.ktor_version}")
                implementation("io.ktor:ktor-client-serialization-js:${Dependency.ktor_version}")

                implementation(kotlinWrappers.js)
                implementation(kotlinWrappers.react)
                implementation(kotlinWrappers.reactDom)
                implementation(kotlinWrappers.reactRouter)
                implementation(kotlinWrappers.emotion.css)
                implementation(kotlinWrappers.emotion.styled)
            }
        }
    }
}

tasks.withType<org.gradle.jvm.tasks.Jar> { duplicatesStrategy = DuplicatesStrategy.INCLUDE}
tasks.named<Jar>("jvmJar") {
    archiveBaseName.set("fiteo")
    archiveVersion.set("1.0.0")
    duplicatesStrategy = DuplicatesStrategy.INCLUDE

    manifest {
        attributes["Main-Class"] = "com.marzec.JvmMainKt"
    }

    doFirst {
        val runtimeClasspath = configurations.getByName("jvmRuntimeClasspath")
        from(runtimeClasspath.map { if (it.isDirectory) it else zipTree(it) })

        val taskName = if (project.hasProperty("isProduction")) {
            "jsBrowserProductionWebpack"
        } else {
            "jsBrowserDevelopmentWebpack"
        }

        val webpackTask = tasks.named<KotlinWebpack>(taskName).get()
        from(File(webpackTask.outputDirectory.get().asFile, webpackTask.mainOutputFileName.get()))
    }

    dependsOn(
        if (project.hasProperty("isProduction")) "jsBrowserProductionWebpack"
        else "jsBrowserDevelopmentWebpack"
    )
}

tasks.getByName<JavaExec>("run") {
    dependsOn("jvmJar")

    val jvmTarget = kotlin.targets.getByName("jvm")
    val jvmMain = jvmTarget.compilations.getByName("main")

    classpath = files(tasks["jvmJar"].outputs.files) +
            jvmMain.runtimeDependencyFiles!! +
            jvmMain.output.allOutputs
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

        buildConfigField(FieldSpec.Type.STRING, "FIREBASE_SERVICE_ACCOUNT_PROD", firebaseServiceAccountProd)
        buildConfigField(FieldSpec.Type.STRING, "FIREBASE_SERVICE_ACCOUNT_TEST", firebaseServiceAccountTest)
    }
}

tasks.jacocoTestReport {

    val coverageSourceDirs = fileTree(
        baseDir = project.projectDir
    ) {
        include(
            "**/src/commonMain/**",
            "**/src/jvmMain/**"
        )
    }

    val classFiles = fileTree(
        baseDir = buildDir
    ) {
        include(
            "**/*.class"
        )
        exclude(
            "**/org/jacoco/**",
            "**/test/com/**"
        )
    }


    classDirectories.setFrom(files(classFiles))
    sourceDirectories.setFrom(files(coverageSourceDirs))

    executionData
        .setFrom(files("${buildDir}/jacoco/jvmTest.exec"))

    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

detekt {
    source = files(
        "src/commonMain/kotlin",
        "src/jvmMain/kotlin",
        "src/jsMain/kotlin",
        "src/commonTest/kotlin",
        "src/jvmTest/kotlin",
        "src/jsTest/kotlin"
    )

    config = files("config/detekt/detekt.yml")
}
