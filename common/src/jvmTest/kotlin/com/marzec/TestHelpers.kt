package com.marzec

import com.google.common.truth.Subject
import com.google.common.truth.Truth
import com.google.common.truth.Truth.assertThat
import com.marzec.data.ExerciseFileMapper
import com.marzec.database.DbSettings
import com.marzec.di.MainModule
import com.marzec.exercises.categories
import com.marzec.exercises.equipment
import com.marzec.exercises.exercises
import com.marzec.exercises.json
import com.marzec.exercises.loginDto
import com.marzec.exercises.registerRequestDto
import com.marzec.exercises.uuidCounter
import com.marzec.io.ExercisesReader
import com.marzec.io.ResourceFileReader
import com.marzec.model.domain.ExercisesData
import com.marzec.model.dto.ExercisesFileDto
import com.marzec.model.dto.LoginRequestDto
import com.marzec.model.dto.RegisterRequestDto
import com.marzec.model.dto.UserDto
import io.ktor.application.Application
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationCall
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.TestApplicationRequest
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import io.mockk.every
import io.mockk.mockk
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import org.flywaydb.core.Flyway
import org.koin.core.module.Module
import org.koin.dsl.module

fun setupDb() {
    DbSettings.dbEndpoint = "jdbc:mysql://localhost:3306/fiteo_test_database?createDatabaseIfNotExist=TRUE"
    DbSettings.dbUser = "root"
    DbSettings.dbPassword = ""

    val flyway = Flyway.configure().dataSource(DbSettings.dbEndpoint, DbSettings.dbUser, DbSettings.dbPassword).load();

    flyway.clean()
    flyway.migrate()
}

fun <T> withDefaultMockTestApplication(
    mockConfiguration: Module.() -> Unit = { },
    applicationModule: Application.(List<Module>) -> Unit = Application::module,
    test: TestApplicationEngine.() -> T
) {
    val withMockConfiguration: Module.() -> Unit = {
        defaultMockConfiguration()
        mockConfiguration()
    }
    withMockTestApplication(withMockConfiguration, applicationModule, test)
}

private fun Module.defaultMockConfiguration() {
    factory(override = true) { uuidCounter }

    factoryMock<ResourceFileReader> { mockk ->
        every { mockk.read(any()) } returns ""
    }
    factoryMock<ExercisesReader> { mockk ->
        every { mockk.parse(any()) } returns ExercisesFileDto(
            null, null, null
        )
    }
    factoryMock<ExerciseFileMapper> {
        every { it.toDomain(any()) } returns ExercisesData(
            categories, exercises, equipment
        )
    }
}

fun <T> withMockTestApplication(
    mockConfiguration: Module.() -> Unit,
    applicationModule: Application.(List<Module>) -> Unit = Application::module,
    test: TestApplicationEngine.() -> T
) {
    setupDb()

    val modules = MainModule.plus(module { mockConfiguration() })
    withTestApplication({ applicationModule(modules) }, test)
}

inline fun <reified T : Any> Module.factoryMock(crossinline mockConfiguration: (T) -> Unit) {
    factory(override = true) {
        mockk<T>().apply { mockConfiguration(this) }
    }
}

inline fun <reified T : Any> assertThatJson(actual: String?): Subject {
    return actual?.let { assertThat(json.decodeFromString<T>(it)) } ?: assertThat(null as Any?)
}

inline fun <reified T> TestApplicationRequest.setBodyJson(dto: T) {
    addHeader("Content-Type", "application/json")
    setBody(json.encodeToString(dto))
}

inline fun <reified REQUEST : Any, reified RESPONSE : Any> testPostEndpoint(
    uri: String,
    dto: REQUEST,
    status: HttpStatusCode,
    responseDto: RESPONSE,
    crossinline authorize: TestApplicationEngine.() -> String? = { null },
    crossinline runRequestsBefore: TestApplicationEngine.(authToken: String?) -> Unit = { }
) {
    withDefaultMockTestApplication {
        val authToken = authorize()
        runRequestsBefore(authToken)
        handleRequest(HttpMethod.Post, uri) {
            setBodyJson(dto)
            authToken?.let { addHeader(Headers.AUTHORIZATION, it) }
        }.apply {
            assertThatJson<RESPONSE>(response.content).isEqualTo(
                responseDto
            )
            assertThat(response.status()).isEqualTo(status)
        }
    }
}

inline fun <reified RESPONSE : Any> testGetEndpoint(
    uri: String,
    status: HttpStatusCode,
    responseDto: RESPONSE,
    crossinline authorize: TestApplicationEngine.() -> String? = { null },
) {
    withDefaultMockTestApplication {
        val authToken = authorize()
        handleRequest(HttpMethod.Get, uri) {
            authToken?.let {
                addHeader(Headers.AUTHORIZATION, it)
            }
        }.apply {
            assertThat(response.status()).isEqualTo(status)
            assertThatJson<RESPONSE>(response.content).isEqualTo(
                responseDto
            )
        }
    }
}

fun TestApplicationEngine.register(dto: RegisterRequestDto = registerRequestDto) {
    handleRequest(HttpMethod.Post, ApiPath.REGISTRATION) {
        setBodyJson(dto)
    }
}

fun TestApplicationEngine.registerAndLogin(dto: LoginRequestDto = loginDto): String {
    register()
    return handleRequest(HttpMethod.Post, ApiPath.LOGIN) {
        setBodyJson(dto)
    }.response.headers[Headers.AUTHORIZATION]!!
}

