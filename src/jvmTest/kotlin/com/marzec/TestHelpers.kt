package com.marzec

import com.google.common.truth.Subject
import com.google.common.truth.Truth.assertThat
import com.marzec.Api.Headers
import com.marzec.cheatday.dto.WeightDto
import com.marzec.database.DbSettings
import com.marzec.di.MainModule
import com.marzec.fiteo.ApiPath
import com.marzec.fiteo.data.ExerciseFileMapper
import com.marzec.fiteo.io.ExercisesReader
import com.marzec.fiteo.io.ResourceFileReader
import com.marzec.fiteo.model.domain.CreateTrainingTemplateDto
import com.marzec.fiteo.model.domain.ExercisesData
import com.marzec.fiteo.model.domain.TrainingDto
import com.marzec.fiteo.model.domain.TrainingTemplateDto
import com.marzec.fiteo.model.dto.CreateExerciseDto
import com.marzec.fiteo.model.dto.ErrorDto
import com.marzec.fiteo.model.dto.ExerciseDto
import com.marzec.fiteo.model.dto.ExercisesFileDto
import com.marzec.fiteo.model.dto.LoginRequestDto
import com.marzec.fiteo.model.dto.RegisterRequestDto
import com.marzec.fiteo.model.dto.UserDto
import com.marzec.todo.dto.TaskDto
import com.marzec.todo.model.CreateTaskDto
import com.marzec.todo.model.UpdateTaskDto
import com.marzec.trader.dto.PaperDto
import com.marzec.trader.dto.PaperTagDto
import com.marzec.trader.dto.TransactionDto
import io.ktor.server.application.*
import io.ktor.http.*
import io.ktor.server.testing.*
import io.mockk.every
import io.mockk.mockk
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import org.flywaydb.core.Flyway
import org.koin.core.module.Module
import org.koin.dsl.module
import java.util.*
import kotlin.reflect.KProperty
import com.marzec.cheatday.ApiPath as CheatApiPath
import com.marzec.fiteo.ApiPath as FiteoApiPath
import com.marzec.todo.ApiPath as TodoApiPath
import com.marzec.trader.ApiPath as TraderApiPath

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
    withMockTestApplication(withDbClear = true, withMockConfiguration, applicationModule, test)
}

fun Module.defaultMockConfiguration() {
    factory { uuidCounter }

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
    withDbClear: Boolean,
    mockConfiguration: Module.() -> Unit,
    applicationModule: Application.(List<Module>) -> Unit = Application::module,
    test: TestApplicationEngine.() -> T
) {
    if (withDbClear) {
        setupDb()
    }

    val modules = MainModule.plus(module { mockConfiguration() })
    withTestApplication({ applicationModule(modules) }, test)
}

inline fun <reified T : Any> Module.factoryMock(crossinline mockConfiguration: (T) -> Unit) {
    factory {
        mockk<T>().apply { mockConfiguration(this) }
    }
}

inline fun <reified T : Any> assertThatJson(actual: String?): Subject {
    return actual?.let { assertThat(json.decodeFromString<T>(it)) } ?: assertThat(null as Any?)
}

inline fun <reified T> TestApplicationRequest.setBodyJson(dto: T) {
    addHeader("Content-Type", "application/json")
    val jsonString = (dto as? String)?.let { it } ?: json.encodeToString(dto)
    setBody(jsonString)
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
) = testEndpoint<REQUEST, RESPONSE>(
    method = method,
    uri = uri,
    dto = dto,
    status = status,
    responseDtoCheck = { assertThat(it).isEqualTo(responseDto) },
    authorize = authorize,
    runRequestsBefore = runRequestsBefore,
    runRequestsAfter = runRequestsAfter
)

inline fun <reified REQUEST : Any, reified RESPONSE : Any> testEndpoint(
    method: HttpMethod,
    uri: String,
    dto: REQUEST?,
    status: HttpStatusCode,
    crossinline responseDtoCheck: (RESPONSE?) -> Unit,
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
            if (response.status() != status) {
                error("Error occurred: " + json.decodeFromString<ErrorDto>(response.content.orEmpty()).reason)
            }
            val responseBody = response.content?.let { json.decodeFromString<RESPONSE>(it) }
            responseDtoCheck(responseBody)
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

inline fun <reified RESPONSE : Any> testGetEndpoint(
    uri: String,
    status: HttpStatusCode,
    crossinline responseDtoCheck: (RESPONSE?) -> Unit,
    crossinline authorize: TestApplicationEngine.() -> String? = { null },
    crossinline runRequestsBefore: TestApplicationEngine.() -> Unit = { },
    crossinline runRequestsAfter: TestApplicationEngine.() -> Unit = { }
) = testEndpoint(
    HttpMethod.Get,
    uri,
    null,
    status,
    responseDtoCheck,
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

fun TestApplicationEngine.login(dto: LoginRequestDto = loginDto): String {
    return handleRequest(HttpMethod.Post, ApiPath.LOGIN) {
        setBodyJson(dto)
    }.response.headers[Headers.AUTHORIZATION]!!
}

fun TestApplicationEngine.loginBearer(dto: LoginRequestDto = LoginRequestDto(email = "mariusz.marzec00@gmail.com", password = "password")): String {
    return handleRequest(HttpMethod.Post, ApiPath.LOGIN_BEARER) {
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

fun TestApplicationEngine.addTask(dto: CreateTaskDto) {
    handleRequest(HttpMethod.Post, TodoApiPath.ADD_TASK.replace("{${Api.Args.ARG_ID}}", "1")) {
        setBodyJson(dto)
        authToken?.let { addHeader(Headers.AUTHORIZATION, it) }
    }
}

fun TestApplicationEngine.addTransaction(dto: TransactionDto) = runAddEndpoint(TraderApiPath.ADD_TRANSACTIONS, dto)

fun TestApplicationEngine.addPaperTag(dto: PaperTagDto) = runAddEndpoint(TraderApiPath.ADD_PAPER_TAG, dto)

fun TestApplicationEngine.addPaper(dto: PaperDto) = runAddEndpoint(TraderApiPath.ADD_PAPER, dto)

fun TestApplicationEngine.addExercise(dto: CreateExerciseDto) = runAddEndpoint(FiteoApiPath.EXERCISES, dto)

fun TestApplicationEngine.updateTask(id: String, dto: UpdateTaskDto) = runPatchEndpoint(id, TodoApiPath.UPDATE_TASK, dto)

inline fun <reified REQUEST> TestApplicationEngine.runPatchEndpoint(id: String, endpointUrl: String, dto: REQUEST) {
    handleRequest(HttpMethod.Patch, endpointUrl.replace("{${Api.Args.ARG_ID}}", id)) {
        setBodyJson(dto)
        authToken?.let { addHeader(Headers.AUTHORIZATION, it) }
    }
}

inline fun <reified REQUEST> TestApplicationEngine.runAddEndpoint(endpointUrl: String, dto: REQUEST) {
    handleRequest(HttpMethod.Post, endpointUrl) {
        setBodyJson(dto)
        authToken?.let { addHeader(Headers.AUTHORIZATION, it) }
    }
}

fun TestApplicationEngine.getTasks(): List<TaskDto> = runGetAllEndpoint(TodoApiPath.TASKS)

fun TestApplicationEngine.getExercises(): List<ExerciseDto> = runGetAllEndpoint(FiteoApiPath.EXERCISES)

fun TestApplicationEngine.papers(): List<PaperDto> = runGetAllEndpoint(TraderApiPath.PAPERS)

fun TestApplicationEngine.tags(): List<PaperTagDto> = runGetAllEndpoint(TraderApiPath.PAPER_TAGS)

inline fun <reified RESPONSE> TestApplicationEngine.runGetAllEndpoint(endpointUrl: String): List<RESPONSE> =
    handleRequest(HttpMethod.Get, endpointUrl) {
        authToken?.let { addHeader(Headers.AUTHORIZATION, it) }
    }.response
        .content
        ?.let { json.decodeFromString<List<RESPONSE>>(it) }.orEmpty()


fun TestApplicationEngine.transactions(): List<TransactionDto> = runGetAllEndpoint(TraderApiPath.TRANSACTIONS)

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

fun TestApplicationEngine.getTemplates(): List<TrainingTemplateDto> {
    return handleRequest(HttpMethod.Get, ApiPath.TRAINING_TEMPLATES) {
        authToken?.let { addHeader(Headers.AUTHORIZATION, it) }
    }.response
        .content
        ?.let { json.decodeFromString<List<TrainingTemplateDto>>(it) }.orEmpty()
}

fun TestApplicationEngine.getTrainings(): List<TrainingDto> {
    return handleRequest(HttpMethod.Get, ApiPath.TRAININGS) {
        authToken?.let { addHeader(Headers.AUTHORIZATION, it) }
    }.response
        .content
        ?.let { json.decodeFromString<List<TrainingDto>>(it) }.orEmpty()
}

fun TestApplicationEngine.getUserCall(): UserDto? {
    return handleRequest(HttpMethod.Get, ApiPath.USER) {
        authToken?.let { addHeader(Headers.AUTHORIZATION, it) }
    }.response
        .content
        ?.let { json.decodeFromString(it) }
}

fun TestApplicationEngine.markAsDone(taskId: Int) {
    val task = getTasks().flatMapTaskDto().first { it.id == taskId }
    val dto = UpdateTaskDto(
        description = task.description,
        parentTaskId = task.parentTaskId,
        priority = task.priority,
        isToDo = false
    )

    handleRequest(HttpMethod.Patch, TodoApiPath.UPDATE_TASK.replace("{${Api.Args.ARG_ID}}", "$taskId")) {
        setBodyJson(dto)
        authToken?.let { addHeader(Headers.AUTHORIZATION, it) }
    }
}

fun List<TaskDto>.flatMapTaskDto(tasks: MutableList<TaskDto> = mutableListOf()): List<TaskDto> {
    forEach {
        tasks.add(it)
        it.subTasks.flatMapTaskDto(tasks)
    }
    return tasks
}
