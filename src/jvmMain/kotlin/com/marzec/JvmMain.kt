package com.marzec

import com.marzec.Api.Auth
import com.marzec.Api.Headers
import com.marzec.cheatday.CheatDayController
import com.marzec.database.DbSettings
import com.marzec.database.UserPrincipal
import com.marzec.database.toPrincipal
import com.marzec.di.Di
import com.marzec.di.MainModule
import com.marzec.extensions.emptyString
import com.marzec.fiteo.ApiPath
import com.marzec.fiteo.api.Controller
import com.marzec.fiteo.model.domain.TestUserSession
import com.marzec.fiteo.model.domain.UserSession
import com.marzec.fiteo.model.dto.LoginRequestDto
import com.marzec.fiteo.model.dto.UserDto
import com.marzec.fiteo.model.http.HttpRequest
import com.marzec.fiteo.model.http.HttpResponse
import com.marzec.sessions.DatabaseSessionStorage
import com.marzec.todo.ToDoApiController
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.sessions.*
import io.ktor.util.pipeline.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.core.module.Module
import org.koin.ktor.ext.Koin
import org.koin.ktor.ext.KoinApplicationStarted
import org.koin.logger.slf4jLogger
import org.slf4j.event.Level
import java.lang.System.currentTimeMillis
import javax.crypto.spec.SecretKeySpec
import kotlin.reflect.KFunction1
import com.marzec.cheatday.ApiPath as CheatDayApiPath
import com.marzec.todo.ApiPath as TodoApiPath
import com.marzec.core.currentMillis

fun main(args: Array<String>) {
    embeddedServer(Netty, commandLineEnvironment(args)).start()
}

@Suppress("unused")
fun Application.module(diModules: List<Module> = listOf(MainModule)) {
    val di = Di(DbSettings.database, Auth.NAME)
    val testDi = Di(DbSettings.testDatabase, Auth.TEST)

    environment.monitor.subscribe(KoinApplicationStarted) {
        di.dataSource.loadData()
        testDi.dataSource.loadData()

        clearSessionsInPeriod(di, testDi)
    }

    install(CallLogging) {
        level = Level.INFO
    }

    install(Koin) {
        slf4jLogger()
        modules(diModules)
    }

    install(DefaultHeaders)

    install(Compression) {
        gzip()
        deflate {
            priority = 10.0
            minimumSize(1024)
        }
    }

    install(CORS) {
        method(HttpMethod.Get)
        method(HttpMethod.Post)
        method(HttpMethod.Delete)
        anyHost()
    }

    install(ContentNegotiation) {
        json(
            contentType = ContentType.Application.Json,
            json = di.json
        )
    }

    install(Sessions) {
        header<UserSession>(Headers.AUTHORIZATION, DatabaseSessionStorage(di.cachedSessionsRepository)) {
            transform(SessionTransportTransformerMessageAuthentication(SecretKeySpec("key".toByteArray(), "AES")))
        }
        header<TestUserSession>(Headers.AUTHORIZATION_TEST, DatabaseSessionStorage(testDi.cachedSessionsRepository)) {
            transform(SessionTransportTransformerMessageAuthentication(SecretKeySpec("key".toByteArray(), "AES")))
        }
    }

    install(Authentication) {
        session<UserSession>(Auth.NAME) {
            challenge {
                call.respond(UnauthorizedResponse())
            }
            validate { session: UserSession ->
                when (val httpResponse = di.api.getUser(createHttpRequest(session.userId))) {
                    is HttpResponse.Success -> httpResponse.data.toPrincipal()
                    else -> null
                }
            }
        }
        session<TestUserSession>(Auth.TEST) {
            challenge {
                call.respond(UnauthorizedResponse())
            }
            validate { session: TestUserSession ->
                when (val httpResponse = testDi.api.getUser(createHttpRequest(session.userId))) {
                    is HttpResponse.Success -> httpResponse.data.toPrincipal()
                    else -> null
                }
            }
        }
    }

    routing {

        static {
            resource("/", "index.html")
            resource("/common.js", "common.js")
        }

        apiSetup(testDi, di) { di ->
            val api: Controller = di.api
            val cheatDayApi: CheatDayController = di.cheatDayController
            val todoController: ToDoApiController = di.todoController

            login(api)
            register(api)
            authenticate(di.authToken) {
                // cheat
                weights(cheatDayApi)
                putWeight(cheatDayApi)
                removeWeight(cheatDayApi)
                updateWeight(cheatDayApi)

                // todo
                todoLists(todoController)
                addTodoList(todoController)
                deleteTodoList(todoController)

                addTask(todoController)
                updateTask(todoController)
                removeTask(todoController)

                // fiteo
                templates(api)
                putTemplate(api)
                removeTemplate(api)
                updateTemplate(api)

                createTraining(api)
                getTraining(api)
                getTrainings(api)
                removeTraining(api)
                updateTraining(api)

                users(api)
                logout()
            }
            equipment(api)
            exercises(api)
            exercisesPage()
            categories(api)
        }
    }
}

private fun clearSessionsInPeriod(di: Di, testDi: Di) {
    val repository = di.cachedSessionsRepository
    val testRepository = testDi.cachedSessionsRepository
    val period = 31 * 24 * 3600L
    GlobalScope.launch {
        while (true) {
            repository.clearOldSessions()
            testRepository.clearOldSessions()
            delay(period)
        }
    }
}

fun Route.createTraining(api: Controller) = getByIdEndpoint(ApiPath.CREATE_TRAINING, api::createTraining)

fun Route.getTraining(api: Controller) = getByIdEndpoint(ApiPath.TRAINING, api::getTraining)

fun Route.getTrainings(api: Controller) = getAllEndpoint(ApiPath.TRAININGS, api::getTrainings)

fun Route.removeTraining(api: Controller) = deleteByIdEndpoint(ApiPath.TRAINING, api::removeTraining)

fun Route.updateTraining(api: Controller) = updateByIdEndpoint(ApiPath.TRAINING, api::updateTraining)

fun Route.templates(api: Controller) = getAllEndpoint(ApiPath.TRAINING_TEMPLATES, api::getTrainingTemplates)

fun Route.putTemplate(api: Controller) = postEndpoint(ApiPath.TRAINING_TEMPLATE, api::addTrainingTemplate)

fun Route.removeTemplate(api: Controller) =
    deleteByIdEndpoint(ApiPath.DELETE_TRAINING_TEMPLATES, api::removeTrainingTemplate)

fun Route.updateTemplate(api: Controller) =
    updateByIdEndpoint(ApiPath.UPDATE_TRAINING_TEMPLATES, api::updateTrainingTemplate)

fun Route.weights(api: CheatDayController) = getAllEndpoint(CheatDayApiPath.WEIGHTS, api::getWeights)

fun Route.putWeight(api: CheatDayController) = postEndpoint(CheatDayApiPath.WEIGHT, api::putWeight)

fun Route.removeWeight(api: CheatDayController) = deleteByIdEndpoint(CheatDayApiPath.REMOVE_WEIGHT, api::removeWeight)

fun Route.updateWeight(api: CheatDayController) = updateByIdEndpoint(CheatDayApiPath.UPDATE_WEIGHT, api::updateWeight)

fun Route.todoLists(api: ToDoApiController) = getByIdEndpoint(TodoApiPath.TODO_LISTS, api::getLists)

fun Route.addTodoList(api: ToDoApiController) = postEndpoint(TodoApiPath.TODO_LIST, api::addList)

fun Route.deleteTodoList(api: ToDoApiController) = deleteByIdEndpoint(TodoApiPath.DELETE_TODO_LIST, api::removeList)

fun Route.addTask(api: ToDoApiController) = postEndpoint(TodoApiPath.ADD_TASK, api::addTask)

fun Route.updateTask(api: ToDoApiController) = updateByIdEndpoint(TodoApiPath.UPDATE_TASK, api::updateTask)

fun Route.removeTask(api: ToDoApiController) = deleteByIdEndpoint(TodoApiPath.DELETE_TASK, api::removeTask)

fun Route.register(api: Controller) = postEndpoint(ApiPath.REGISTRATION, api::postRegister)

fun Route.login(api: Controller) {
    post(ApiPath.LOGIN) {
        val loginRequestDto = call.receiveOrNull<LoginRequestDto>()
        val httpResponse = api.postLogin(HttpRequest(loginRequestDto))
        if (httpResponse is HttpResponse.Success<UserDto>) {
            if (call.request.uri.contains("test/")) {
                call.sessions.set(
                    Headers.AUTHORIZATION_TEST, TestUserSession(httpResponse.data.id, currentMillis())
                )
            } else {
                call.sessions.set(Headers.AUTHORIZATION, UserSession(httpResponse.data.id, currentMillis()))
            }
        }
        dispatch(httpResponse)
    }
}

fun Route.logout() {
    get(ApiPath.LOGOUT) {
        if (call.request.uri.contains("test/")) {
            call.sessions.clear<TestUserSession>()
        } else {
            call.sessions.clear<UserSession>()
        }
        dispatch(HttpResponse.Success(Unit))
    }
}

fun Route.users(api: Controller) = getBySessionEndpoint(ApiPath.USER, api::getUser)

fun Route.exercises(api: Controller) = getAllEndpoint(ApiPath.EXERCISES, api::getExercises)

fun Route.exercisesPage() {
    get(ApiPath.EXERCISES_PAGE) {
        call.respondText(
            this::class.java.classLoader.getResource("index.html")!!.readText(),
            ContentType.Text.Html
        )
    }
}

fun Route.categories(api: Controller) = getAllEndpoint(ApiPath.CATEGORIES, api::getCategories)

fun Route.equipment(api: Controller) = getAllEndpoint(ApiPath.EQUIPMENT, api::getEquipment)

fun Route.apiSetup(testDi: Di, prodDi: Di, setup: Route.(di: Di) -> Unit) {

    route(ApiPath.TEST_API_PREFIX) {
        setup(testDi)
    }
    setup(prodDi)
}

private suspend inline fun <reified T : Any> PipelineContext<Unit, ApplicationCall>.dispatch(response: HttpResponse<T>) {
    when (response) {
        is HttpResponse.Success -> {
            response.headers.forEach { (header, value) ->
                call.response.headers.append(header, value)
            }
            call.respond(response.data)
        }
        is HttpResponse.Error -> {
            call.respond(HttpStatusCode.fromValue(response.httpStatusCode), response.data)
        }
    }
}

private inline fun <reified T : Any> Route.getByIdEndpoint(
    path: String,
    apiFunRef: KFunction1<HttpRequest<Unit>, HttpResponse<T>>
) {
    get(path) {
        val httpRequest = HttpRequest(
            data = Unit,
            parameters = mapOf(
                Api.Args.ARG_ID to call.parameters[Api.Args.ARG_ID]
            ),
            sessions = mapOf(Api.Args.ARG_USER_ID to call.principal<UserPrincipal>()?.id.toString())
        )
        dispatch(apiFunRef(httpRequest))
    }
}

private inline fun <reified T : Any> Route.getAllEndpoint(
    path: String,
    apiFunRef: KFunction1<HttpRequest<Unit>, HttpResponse<List<T>>>
) {
    get(path) {
        (call.principal<UserPrincipal>()?.id ?: emptyString()).toString()
        val httpRequest = createHttpRequest(call.principal<UserPrincipal>()?.id)
        dispatch(apiFunRef(httpRequest))
    }
}

private inline fun <reified T : Any> Route.deleteByIdEndpoint(
    path: String,
    apiFunRef: KFunction1<HttpRequest<Unit>, HttpResponse<T>>
) {
    delete(path) {
        val httpRequest = HttpRequest(
            data = Unit,
            parameters = mapOf(
                Api.Args.ARG_ID to call.parameters[Api.Args.ARG_ID]
            ),
            sessions = mapOf(Api.Args.ARG_USER_ID to call.principal<UserPrincipal>()?.id.toString())
        )
        dispatch(apiFunRef(httpRequest))
    }
}

private inline fun <reified REQUEST : Any, reified RESPONSE : Any> Route.updateByIdEndpoint(
    path: String,
    apiFunRef: KFunction1<HttpRequest<REQUEST>, HttpResponse<RESPONSE>>
) {
    patch(path) {
        val dto = call.receive<REQUEST>()
        val taskId = call.parameters[Api.Args.ARG_ID]
        val httpRequest = HttpRequest(
            data = dto,
            parameters = mapOf(pair = Api.Args.ARG_ID to taskId),
            sessions = mapOf(Api.Args.ARG_USER_ID to call.principal<UserPrincipal>()?.id.toString())
        )
        dispatch(apiFunRef(httpRequest))
    }
}

private inline fun <reified REQUEST : Any, reified RESPONSE : Any> Route.postEndpoint(
    path: String,
    apiFunRef: KFunction1<HttpRequest<REQUEST>, HttpResponse<RESPONSE>>
) {
    post(path) {
        val dto = call.receive<REQUEST>()
        val taskId = call.parameters[Api.Args.ARG_ID]
        val httpRequest = HttpRequest(
            data = dto,
            parameters = mapOf(
                pair = Api.Args.ARG_ID to taskId,
            ),
            sessions = mapOf(Api.Args.ARG_USER_ID to call.principal<UserPrincipal>()?.id.toString())
        )
        dispatch(apiFunRef(httpRequest))
    }
}

private inline fun <reified T : Any> Route.getBySessionEndpoint(
    path: String,
    apiFunRef: KFunction1<HttpRequest<Unit>, HttpResponse<T>>
) {
    get(path) {
        val id = call.principal<UserPrincipal>()?.id
        val httpRequest = createHttpRequest(id)
        dispatch(apiFunRef(httpRequest))
    }
}

private fun createHttpRequest(userId: Int?): HttpRequest<Unit> = HttpRequest(
    data = Unit,
    parameters = emptyMap(),
    sessions = mapOf(Api.Args.ARG_USER_ID to userId.toString())
)
