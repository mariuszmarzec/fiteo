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
import com.marzec.fiteo.model.dto.ErrorDto
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
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.server.application.*
import io.ktor.http.*
import io.ktor.server.testing.*
import io.mockk.every
import io.mockk.mockk
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import org.flywaydb.core.Flyway
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.mp.KoinPlatformTools
import java.util.*
import kotlin.reflect.KProperty
import com.marzec.cheatday.ApiPath as CheatApiPath
import com.marzec.todo.ApiPath as TodoApiPath
import com.marzec.trader.ApiPath as TraderApiPath

//Workaround
//https://youtrack.jetbrains.com/issue/KT-45505/IAE-suspend-default-lambda-X-cannot-be-inlined-use-a-function-reference-instead-with-crossinline-suspend-lambda-and-default


suspend fun ApplicationTestBuilder.defLambda() = Unit
suspend fun ApplicationTestBuilder.defStringLambda(): String? = null

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
    test: suspend ApplicationTestBuilder.() -> T
) {
    val withMockConfiguration: Module.() -> Unit = {
        defaultMockConfiguration()
        mockConfiguration()
    }
    withMockTestApplication(withDbClear = true, withMockConfiguration, test)
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
    test: suspend ApplicationTestBuilder.() -> T
) {
    if (withDbClear) {
        setupDb()
    }

    val diModules = MainModule.plus(module { mockConfiguration() })

    KoinPlatformTools.defaultContext().getOrNull()?.close()
    startKoin { modules(diModules) }.koin

    testApplication {
        test()
    }

    stopKoin()
}

inline fun <reified T : Any> Module.factoryMock(crossinline mockConfiguration: (T) -> Unit) {
    factory {
        mockk<T>().apply { mockConfiguration(this) }
    }
}

inline fun <reified T : Any> assertThatJson(actual: String?): Subject {
    return actual?.let { assertThat(json.decodeFromString<T>(it)) } ?: assertThat(null as Any?)
}

inline fun <reified T> HttpRequestBuilder.setBodyJson(dto: T) {
    header("Content-Type", "application/json")
    setBody(json.encodeToString(dto))
}

inline fun <reified REQUEST : Any, reified RESPONSE : Any> testPostEndpoint(
    uri: String,
    dto: REQUEST,
    status: HttpStatusCode,
    responseDto: RESPONSE,
    crossinline authorize: suspend ApplicationTestBuilder.() -> String? = ApplicationTestBuilder::defStringLambda,
    crossinline runRequestsBefore: suspend ApplicationTestBuilder.() -> Unit = ApplicationTestBuilder::defLambda,
    crossinline runRequestsAfter: suspend ApplicationTestBuilder.() -> Unit = ApplicationTestBuilder::defLambda
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
    crossinline authorize: suspend ApplicationTestBuilder.() -> String? = ApplicationTestBuilder::defStringLambda,
    crossinline runRequestsBefore: suspend ApplicationTestBuilder.() -> Unit = ApplicationTestBuilder::defLambda,
    crossinline runRequestsAfter: suspend ApplicationTestBuilder.() -> Unit = ApplicationTestBuilder::defLambda
) {
    withDefaultMockTestApplication {
        authToken = authorize()
        runRequestsBefore()
        this.client.request(uri) {
            this.method = method
            dto?.let { setBodyJson(it) }
            authToken?.let { header(Headers.AUTHORIZATION, it) }
        }.let { response ->
            if (response.status != status) {
                error(json.decodeFromString<ErrorDto>(response.bodyAsText()).reason)
            }
            println(response.bodyAsText())
            assertThatJson<RESPONSE>(response.bodyAsText()).isEqualTo(
                responseDto
            )
            assertThat(response.status).isEqualTo(status)
            runRequestsAfter()
        }
    }
}

inline fun <reified RESPONSE : Any> testGetEndpoint(
    uri: String,
    status: HttpStatusCode,
    responseDto: RESPONSE,
    crossinline authorize: suspend ApplicationTestBuilder.() -> String? = ApplicationTestBuilder::defStringLambda,
    crossinline runRequestsBefore: suspend ApplicationTestBuilder.() -> Unit = ApplicationTestBuilder::defLambda,
    crossinline runRequestsAfter: suspend ApplicationTestBuilder.() -> Unit = ApplicationTestBuilder::defLambda
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
    crossinline authorize: suspend ApplicationTestBuilder.() -> String? = ApplicationTestBuilder::defStringLambda,
    crossinline runRequestsBefore: suspend ApplicationTestBuilder.() -> Unit = ApplicationTestBuilder::defLambda,
    crossinline runRequestsAfter: suspend ApplicationTestBuilder.() -> Unit = ApplicationTestBuilder::defLambda
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
    crossinline authorize: suspend ApplicationTestBuilder.() -> String? = ApplicationTestBuilder::defStringLambda,
    crossinline runRequestsBefore: suspend ApplicationTestBuilder.() -> Unit = ApplicationTestBuilder::defLambda,
    crossinline runRequestsAfter: suspend ApplicationTestBuilder.() -> Unit = ApplicationTestBuilder::defLambda
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

var ApplicationTestBuilder.authToken: String? by FieldProperty<ApplicationTestBuilder, String?> { null }

suspend inline fun <reified DTO> ApplicationTestBuilder.request(
    method: HttpMethod,
    uri: String,
    dto: DTO
): HttpResponse =
    client.request(uri) {
        this.method = method
        setBodyJson(dto)
    }

suspend inline fun <reified DTO> ApplicationTestBuilder.requestWithAuth(
    method: HttpMethod,
    uri: String,
    dto: DTO
): HttpResponse =
    client.request(uri) {
        this.method = method
        setBodyJson(dto)
        authToken?.let { header(Headers.AUTHORIZATION, it) }
    }

suspend inline fun <reified DTO, reified RESPONSE> ApplicationTestBuilder.postWithAuth(
    uri: String,
    dto: DTO
): RESPONSE =
    client.request(uri) {
        this.method = HttpMethod.Post
        setBodyJson(dto)
        authToken?.let { header(Headers.AUTHORIZATION, it) }
    }.bodyAsText().let { json.decodeFromString<RESPONSE>(it) }

suspend inline fun <reified DTO, reified RESPONSE> ApplicationTestBuilder.patchWithAuth(
    uri: String,
    dto: DTO
): RESPONSE =
    client.request(uri) {
        this.method = HttpMethod.Patch
        setBodyJson(dto)
        authToken?.let { header(Headers.AUTHORIZATION, it) }
    }.bodyAsText().let { json.decodeFromString<RESPONSE>(it) }

suspend inline fun <reified RESPONSE> ApplicationTestBuilder.getWithAuth(uri: String): RESPONSE =
    client.request(uri) {
        this.method = HttpMethod.Get
        authToken?.let { header(Headers.AUTHORIZATION, it) }
    }.bodyAsText().let { json.decodeFromString<RESPONSE>(it) }

suspend fun ApplicationTestBuilder.register(dto: RegisterRequestDto = registerRequestDto) {
    request(HttpMethod.Post, ApiPath.REGISTRATION, dto)
}

suspend fun ApplicationTestBuilder.registerAndLogin(dto: LoginRequestDto = loginDto): String {
    register()
    return login()
}

suspend fun ApplicationTestBuilder.login(dto: LoginRequestDto = loginDto): String =
    request(HttpMethod.Post, ApiPath.LOGIN, dto).headers[Headers.AUTHORIZATION]!!

suspend fun ApplicationTestBuilder.addWeight(dto: WeightDto) {
    postWithAuth<WeightDto, Unit>(CheatApiPath.WEIGHT, dto)
}

suspend fun ApplicationTestBuilder.getWeights(): List<WeightDto> =
    getWithAuth(CheatApiPath.WEIGHTS)

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

suspend fun ApplicationTestBuilder.addTask(dto: CreateTaskDto) {
    postWithAuth<CreateTaskDto, Unit>(TodoApiPath.ADD_TASK.replace("{${Api.Args.ARG_ID}}", "1"), dto)
}

suspend fun ApplicationTestBuilder.addTransaction(dto: TransactionDto) =
    runAddEndpoint(TraderApiPath.ADD_TRANSACTIONS, dto)

suspend fun ApplicationTestBuilder.addPaperTag(dto: PaperTagDto) = runAddEndpoint(TraderApiPath.ADD_PAPER_TAG, dto)

suspend fun ApplicationTestBuilder.addPaper(dto: PaperDto) = runAddEndpoint(TraderApiPath.ADD_PAPER, dto)

suspend fun ApplicationTestBuilder.updateTask(id: String, dto: UpdateTaskDto) =
    runPatchEndpoint(id, TodoApiPath.UPDATE_TASK, dto)

suspend inline fun <reified REQUEST> ApplicationTestBuilder.runPatchEndpoint(
    id: String,
    endpointUrl: String,
    dto: REQUEST
) {
    requestWithAuth(HttpMethod.Patch, endpointUrl.replace("{${Api.Args.ARG_ID}}", id), dto)
}

suspend inline fun <reified REQUEST> ApplicationTestBuilder.runAddEndpoint(endpointUrl: String, dto: REQUEST) {
    postWithAuth<REQUEST, Unit>(endpointUrl, dto)
}

suspend fun ApplicationTestBuilder.getTasks(): List<TaskDto> = runGetAllEndpoint(TodoApiPath.TASKS)

suspend fun ApplicationTestBuilder.papers(): List<PaperDto> = runGetAllEndpoint(TraderApiPath.PAPERS)

suspend fun ApplicationTestBuilder.tags(): List<PaperTagDto> = runGetAllEndpoint(TraderApiPath.PAPER_TAGS)

suspend inline fun <reified RESPONSE> ApplicationTestBuilder.runGetAllEndpoint(endpointUrl: String): List<RESPONSE> =
    getWithAuth<List<RESPONSE>>(endpointUrl)


suspend fun ApplicationTestBuilder.transactions(): List<TransactionDto> = runGetAllEndpoint(TraderApiPath.TRANSACTIONS)

suspend fun ApplicationTestBuilder.putTemplate(dto: CreateTrainingTemplateDto) {
    postWithAuth<CreateTrainingTemplateDto, Unit>(ApiPath.TRAINING_TEMPLATE, dto)
}

suspend fun ApplicationTestBuilder.createTraining(trainingTemplateId: String): TrainingDto =
    getWithAuth(
        ApiPath.CREATE_TRAINING.replace("{${Api.Args.ARG_ID}}", trainingTemplateId)
    )

suspend fun ApplicationTestBuilder.getTemplates(): List<TrainingTemplateDto> =
    getWithAuth(ApiPath.TRAINING_TEMPLATES)

suspend fun ApplicationTestBuilder.getTrainings(): List<TrainingDto> = getWithAuth(ApiPath.TRAININGS)

suspend fun ApplicationTestBuilder.markAsDone(taskId: Int) {
    val task = getTasks().flatMapTaskDto().first { it.id == taskId }
    val dto = UpdateTaskDto(
        description = task.description,
        parentTaskId = task.parentTaskId,
        priority = task.priority,
        isToDo = false
    )

    patchWithAuth<UpdateTaskDto, Unit>(TodoApiPath.UPDATE_TASK.replace("{${Api.Args.ARG_ID}}", "$taskId"), dto)
}

fun List<TaskDto>.flatMapTaskDto(tasks: MutableList<TaskDto> = mutableListOf()): List<TaskDto> {
    forEach {
        tasks.add(it)
        it.subTasks.flatMapTaskDto(tasks)
    }
    return tasks
}
