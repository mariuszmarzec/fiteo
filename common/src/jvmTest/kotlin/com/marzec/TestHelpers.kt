package com.marzec

import com.marzec.cheatday.ApiPath as CheatApiPath
import com.marzec.todo.ApiPath as TodoApiPath
import com.google.common.truth.Subject
import com.google.common.truth.Truth.assertThat
import com.marzec.cheatday.dto.WeightDto
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
import com.marzec.fiteo.ApiPath
import com.marzec.Api.Headers
import com.marzec.fiteo.io.ExercisesReader
import com.marzec.fiteo.io.ResourceFileReader
import com.marzec.model.domain.CreateTrainingTemplateDto
import com.marzec.model.domain.ExercisesData
import com.marzec.model.domain.TrainingDto
import com.marzec.model.domain.TrainingTemplateDto
import com.marzec.model.dto.ExercisesFileDto
import com.marzec.model.dto.LoginRequestDto
import com.marzec.model.dto.RegisterRequestDto
import com.marzec.todo.dto.CreateTodoListDto
import com.marzec.todo.dto.ToDoListDto
import com.marzec.todo.model.CreateTaskDto
import io.ktor.application.Application
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.TestApplicationRequest
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import io.mockk.every
import io.mockk.mockk
import java.util.*
import kotlin.reflect.KProperty
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
    crossinline runRequestsBefore: TestApplicationEngine.() -> Unit = { },
    crossinline runRequestsAfter: TestApplicationEngine.() -> Unit = { }
) = testEndpoint(
    HttpMethod.Post,
    uri,
    dto,
    status,
    responseDto,
    authorize,
    runRequestsBefore,
    runRequestsAfter
)

inline fun <reified REQUEST : Any, reified RESPONSE : Any> testEndpoint(
    method: HttpMethod,
    uri: String,
    dto: REQUEST?,
    status: HttpStatusCode,
    responseDto: RESPONSE,
    crossinline authorize: TestApplicationEngine.() -> String? = { null },
    crossinline runRequestsBefore: TestApplicationEngine.() -> Unit = { },
    crossinline runRequestsAfter: TestApplicationEngine.() -> Unit = { }
) {
    withDefaultMockTestApplication {
        authToken = authorize()
        runRequestsBefore()
        handleRequest(method, uri) {
            dto?.let { setBodyJson(it) }
            authToken?.let { addHeader(Headers.AUTHORIZATION, it) }
        }.apply {
            assertThatJson<RESPONSE>(response.content).isEqualTo(
                responseDto
            )
            assertThat(response.status()).isEqualTo(status)
            runRequestsAfter()
        }
    }
}

inline fun <reified RESPONSE : Any> testGetEndpoint(
    uri: String,
    status: HttpStatusCode,
    responseDto: RESPONSE,
    crossinline authorize: TestApplicationEngine.() -> String? = { null },
    crossinline runRequestsBefore: TestApplicationEngine.() -> Unit = { },
    crossinline runRequestsAfter: TestApplicationEngine.() -> Unit = { }
) = testEndpoint(
    HttpMethod.Get,
    uri,
    null,
    status,
    responseDto,
    authorize,
    runRequestsBefore,
    runRequestsAfter
)

inline fun <reified RESPONSE : Any> testDeleteEndpoint(
    uri: String,
    status: HttpStatusCode,
    responseDto: RESPONSE,
    crossinline authorize: TestApplicationEngine.() -> String? = { null },
    crossinline runRequestsBefore: TestApplicationEngine.() -> Unit = { },
    crossinline runRequestsAfter: TestApplicationEngine.() -> Unit = { }
) = testEndpoint(
    HttpMethod.Delete,
    uri,
    null,
    status,
    responseDto,
    authorize,
    runRequestsBefore,
    runRequestsAfter
)

inline fun <reified REQUEST : Any, reified RESPONSE : Any> testPatchEndpoint(
    uri: String,
    dto: REQUEST,
    status: HttpStatusCode,
    responseDto: RESPONSE,
    crossinline authorize: TestApplicationEngine.() -> String? = { null },
    crossinline runRequestsBefore: TestApplicationEngine.() -> Unit = { },
    crossinline runRequestsAfter: TestApplicationEngine.() -> Unit = { }
) = testEndpoint(
    HttpMethod.Patch,
    uri,
    dto,
    status,
    responseDto,
    authorize,
    runRequestsBefore,
    runRequestsAfter
)

var TestApplicationEngine.authToken: String? by FieldProperty<TestApplicationEngine, String?> { null }

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

fun TestApplicationEngine.addWeight(dto: WeightDto) {
    handleRequest(HttpMethod.Post, CheatApiPath.WEIGHT) {
        setBodyJson(dto)
        authToken?.let { addHeader(Headers.AUTHORIZATION, it) }
    }
}

fun TestApplicationEngine.getWeights(): List<WeightDto> =
    handleRequest(HttpMethod.Get, CheatApiPath.WEIGHTS) {
        authToken?.let { addHeader(Headers.AUTHORIZATION, it) }
    }.response
        .content
        ?.let { json.decodeFromString<List<WeightDto>>(it) }.orEmpty()

class FieldProperty<R, T>(
    val initializer: (R) -> T = { throw IllegalStateException("Not initialized.") }
) {
    private val map = WeakHashMap<R, T>()

    operator fun getValue(thisRef: R, property: KProperty<*>): T =
        map[thisRef] ?: setValue(thisRef, property, initializer(thisRef))

    operator fun setValue(thisRef: R, property: KProperty<*>, value: T): T {
        map[thisRef] = value
        return value
    }
}

fun TestApplicationEngine.addTodoList(dto: CreateTodoListDto) {
    handleRequest(HttpMethod.Post, TodoApiPath.TODO_LIST) {
        setBodyJson(dto)
        authToken?.let { addHeader(Headers.AUTHORIZATION, it) }
    }
}

fun TestApplicationEngine.addTask(listId: Int, dto: CreateTaskDto) {
    handleRequest(HttpMethod.Post, TodoApiPath.ADD_TASK.replace("{${Api.Args.ARG_ID}}", "$listId")) {
        setBodyJson(dto)
        authToken?.let { addHeader(Headers.AUTHORIZATION, it) }
    }
}

fun TestApplicationEngine.getTodoLists(): List<ToDoListDto> {
    return handleRequest(HttpMethod.Get, TodoApiPath.TODO_LISTS) {
        authToken?.let { addHeader(Headers.AUTHORIZATION, it) }
    }.response
        .content
        ?.let { json.decodeFromString<List<ToDoListDto>>(it) }.orEmpty()
}

fun TestApplicationEngine.putTemplate(dto: CreateTrainingTemplateDto) {
    handleRequest(HttpMethod.Post, ApiPath.TRAINING_TEMPLATE) {
        setBodyJson(dto)
        authToken?.let { addHeader(Headers.AUTHORIZATION, it) }
    }
}

fun TestApplicationEngine.createTraining(trainingTemplateId: String): TrainingDto {
    return handleRequest(
        HttpMethod.Get,
        ApiPath.CREATE_TRAINING.replace("{${Api.Args.ARG_ID}}", trainingTemplateId)
    ) {
        authToken?.let { addHeader(Headers.AUTHORIZATION, it) }
    }.response
        .content
        .let { json.decodeFromString(it!!) }
}

fun TestApplicationEngine.getTemplates() : List<TrainingTemplateDto> {
    return handleRequest(HttpMethod.Get, ApiPath.TRAINING_TEMPLATES) {
        authToken?.let { addHeader(Headers.AUTHORIZATION, it) }
    }.response
        .content
        ?.let { json.decodeFromString<List<TrainingTemplateDto>>(it) }.orEmpty()
}

fun TestApplicationEngine.getTrainings() : List<TrainingDto> {
    return handleRequest(HttpMethod.Get, ApiPath.TRAININGS) {
        authToken?.let { addHeader(Headers.AUTHORIZATION, it) }
    }.response
        .content
        ?.let { json.decodeFromString<List<TrainingDto>>(it) }.orEmpty()
}
