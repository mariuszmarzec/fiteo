package com.marzec

import com.marzec.cheatday.ApiPath as CheatDayApiPath
import com.marzec.todo.ApiPath as TodoApiPath
import io.ktor.server.netty.EngineMain as NettyEngineMain
import com.marzec.api.Controller
import com.marzec.cheatday.CheatDayController
import com.marzec.cheatday.dto.PutWeightDto
import com.marzec.cheatday.dto.WeightDto
import com.marzec.database.DbSettings
import com.marzec.database.UserPrincipal
import com.marzec.database.toPrincipal
import com.marzec.di.Di
import com.marzec.di.MainModule
import com.marzec.extensions.emptyString
import com.marzec.model.domain.CreateTrainingTemplateDto
import com.marzec.model.domain.TestUserSession
import com.marzec.model.domain.UserSession
import com.marzec.model.dto.LoginRequestDto
import com.marzec.model.dto.RegisterRequestDto
import com.marzec.model.dto.UserDto
import com.marzec.model.http.HttpRequest
import com.marzec.model.http.HttpResponse
import com.marzec.sessions.DatabaseSessionStorage
import com.marzec.todo.api.ToDoApiController
import com.marzec.todo.dto.CreateTodoListDto
import com.marzec.todo.model.CreateTaskDto
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.UnauthorizedResponse
import io.ktor.auth.authenticate
import io.ktor.auth.principal
import io.ktor.auth.session
import io.ktor.features.CORS
import io.ktor.features.CallLogging
import io.ktor.features.Compression
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.features.deflate
import io.ktor.features.gzip
import io.ktor.features.minimumSize
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.resource
import io.ktor.http.content.static
import io.ktor.request.receive
import io.ktor.request.receiveOrNull
import io.ktor.request.uri
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.delete
import io.ktor.routing.get
import io.ktor.routing.patch
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.serialization.json
import io.ktor.sessions.SessionTransportTransformerMessageAuthentication
import io.ktor.sessions.Sessions
import io.ktor.sessions.clear
import io.ktor.sessions.header
import io.ktor.sessions.sessions
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.pipeline.PipelineContext
import java.lang.System.currentTimeMillis
import javax.crypto.spec.SecretKeySpec
import kotlin.reflect.KFunction1
import org.koin.ktor.ext.Koin
import org.koin.ktor.ext.KoinApplicationStarted
import org.koin.logger.slf4jLogger
import org.slf4j.event.Level

fun main(args: Array<String>): Unit = NettyEngineMain.main(args)

@Suppress("unused")
fun Application.module(testing: Boolean = false) {
    val di = Di(DbSettings.database, Auth.NAME)
    val testDi = Di(DbSettings.testDatabase, Auth.TEST)

    environment.monitor.subscribe(KoinApplicationStarted) {
        di.dataSource.loadData()
        testDi.dataSource.loadData()
    }

    install(CallLogging) {
        level = Level.INFO
    }

    install(Koin) {
        slf4jLogger()
        modules(MainModule)
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
                when (val httpResponse = di.api.getUser(wrapAsRequest(ApiPath.ARG_ID, session.userId))) {
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
                when (val httpResponse = testDi.api.getUser(wrapAsRequest(ApiPath.ARG_ID, session.userId))) {
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
            categories(api)

            for (it in allRoutes(this)) {
                println(it)
            }
        }
    }
}

fun allRoutes(root: Route): List<Route> {
    return listOf(root) + root.children.flatMap { allRoutes(it) }
}

fun Route.createTraining(api: Controller) {
    get(ApiPath.CREATE_TRAINING) {
        val httpRequest = HttpRequest(
            data = Unit,
            parameters = mapOf(
                ApiPath.ARG_ID to call.parameters[ApiPath.ARG_ID],
                ApiPath.ARG_USER_ID to (call.principal<UserPrincipal>()?.id ?: emptyString()).toString()
            )
        )
        dispatch(api.createTraining(httpRequest))
    }
}

fun Route.getTraining(api: Controller) = getByIdEndpoint(ApiPath.TRAINING, api::getTraining)

fun Route.getTrainings(api: Controller) = getAllEndpoint(ApiPath.TRAININGS, api::getTrainings)

fun Route.removeTraining(api: Controller) = deleteByIdEndpoint(ApiPath.TRAINING, api::removeTraining)

fun Route.updateTraining(api: Controller) = updateByIdEndpoint(ApiPath.TRAINING, api::updateTraining)

fun Route.templates(api: Controller) {
    get(ApiPath.TRAINING_TEMPLATES) {
        val httpRequest = wrapAsRequest(ApiPath.ARG_ID, call.principal<UserPrincipal>()?.id ?: emptyString())
        dispatch(api.getTrainingTemplates(httpRequest))
    }
}

fun Route.putTemplate(api: Controller) {
    post(ApiPath.TRAINING_TEMPLATE) {
        val dto = call.receive<CreateTrainingTemplateDto>()
        val httpRequest = wrapAsRequest(
            dto,
            ApiPath.ARG_ID,
            call.principal<UserPrincipal>()?.id ?: emptyString()
        )
        dispatch(api.addTrainingTemplate(httpRequest))
    }
}

fun Route.removeTemplate(api: Controller) =
    deleteByIdEndpoint(ApiPath.DELETE_TRAINING_TEMPLATES, api::removeTrainingTemplate)

fun Route.updateTemplate(api: Controller) {
    patch(ApiPath.UPDATE_TRAINING_TEMPLATES) {
        val dto = call.receive<CreateTrainingTemplateDto>()
        val httpRequest = wrapAsRequest(
            dto,
            ApiPath.ARG_USER_ID,
            call.principal<UserPrincipal>()?.id ?: emptyString()
        )
        dispatch(api.updateTrainingTemplate(httpRequest))
    }
}

fun Route.weights(api: CheatDayController) {
    get(CheatDayApiPath.WEIGHTS) {
        val httpRequest = wrapAsRequest(ApiPath.ARG_ID, call.principal<UserPrincipal>()?.id ?: emptyString())
        dispatch(api.getWeights(httpRequest))
    }
}

fun Route.putWeight(api: CheatDayController) {
    post(CheatDayApiPath.WEIGHT) {
        val putWeightDto = call.receive<PutWeightDto>()
        val httpRequest = wrapAsRequest(
            putWeightDto,
            ApiPath.ARG_ID,
            call.principal<UserPrincipal>()?.id ?: emptyString()
        )
        dispatch(api.putWeight(httpRequest))
    }
}

fun Route.removeWeight(api: CheatDayController) {
    delete(CheatDayApiPath.REMOVE_WEIGHT) {
        val weightId = call.parameters[TodoApiPath.ARG_ID]
        val httpRequest = HttpRequest(
            Unit,
            mapOf(
                ApiPath.ARG_USER_ID to call.principal<UserPrincipal>()?.id?.toString(),
                ApiPath.ARG_ID to weightId,
            )
        )
        dispatch(api.removeWeight(httpRequest))
    }
}

fun Route.updateWeight(api: CheatDayController) {
    patch(CheatDayApiPath.UPDATE_WEIGHT) {
        val weightDto = call.receive<WeightDto>()
        val httpRequest = wrapAsRequest(
            weightDto,
            ApiPath.ARG_USER_ID,
            call.principal<UserPrincipal>()?.id ?: emptyString()
        )
        dispatch(api.updateWeight(httpRequest))
    }
}

fun Route.todoLists(api: ToDoApiController) {
    get(TodoApiPath.TODO_LISTS) {
        val httpRequest = wrapAsRequest(ApiPath.ARG_ID, call.principal<UserPrincipal>()?.id ?: emptyString())
        dispatch(api.getLists(httpRequest))
    }
}

fun Route.addTodoList(api: ToDoApiController) {
    post(TodoApiPath.TODO_LIST) {
        val createDto = call.receive<CreateTodoListDto>()
        val httpRequest = wrapAsRequest(
            createDto,
            ApiPath.ARG_ID,
            call.principal<UserPrincipal>()?.id ?: emptyString()
        )
        dispatch(api.addList(httpRequest))
    }
}

fun Route.deleteTodoList(api: ToDoApiController) {
    delete(TodoApiPath.DELETE_TODO_LIST) {
        val todoList = call.parameters[TodoApiPath.ARG_ID]
        val httpRequest = HttpRequest(
            Unit,
            mapOf(
                ApiPath.ARG_USER_ID to call.principal<UserPrincipal>()?.id?.toString(),
                ApiPath.ARG_ID to todoList,
            )
        )
        dispatch(api.removeList(httpRequest))
    }

}

fun Route.addTask(api: ToDoApiController) {
    post(TodoApiPath.ADD_TASK) {
        val createDto = call.receive<CreateTaskDto>()
        val todoList = call.parameters[TodoApiPath.ARG_ID]
        val httpRequest = HttpRequest(
            createDto,
            mapOf(
                ApiPath.ARG_USER_ID to call.principal<UserPrincipal>()?.id?.toString(),
                ApiPath.ARG_ID to todoList,
            )
        )
        dispatch(api.addTask(httpRequest))
    }
}

fun Route.updateTask(api: ToDoApiController) = updateByIdEndpoint(TodoApiPath.UPDATE_TASK, api::updateTask)

fun Route.removeTask(api: ToDoApiController) {
    delete(TodoApiPath.DELETE_TASK) {
        val taskId = call.parameters[ApiPath.ARG_ID]
        val httpRequest = HttpRequest(
            data = Unit,
            parameters = mapOf(
                ApiPath.ARG_USER_ID to call.principal<UserPrincipal>()?.id?.toString(),
                ApiPath.ARG_ID to taskId,
            )
        )
        dispatch(api.removeTask(httpRequest))
    }
}

fun Route.register(api: Controller) {
    post(ApiPath.REGISTRATION) {
        val registerRequestDto = call.receive<RegisterRequestDto>()
        val httpResponse = api.postRegister(HttpRequest(registerRequestDto))
        dispatch(httpResponse)
    }
}

fun Route.login(api: Controller) {
    post(ApiPath.LOGIN) {
        val loginRequestDto = call.receiveOrNull<LoginRequestDto>()
        val httpResponse = api.postLogin(HttpRequest(loginRequestDto))
        if (httpResponse is HttpResponse.Success<UserDto>) {
            if (call.request.uri.contains("test/")) {
                call.sessions.set(Headers.AUTHORIZATION_TEST, TestUserSession(httpResponse.data.id, currentTimeMillis()))
            } else {
                call.sessions.set(Headers.AUTHORIZATION, UserSession(httpResponse.data.id, currentTimeMillis()))
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

@KtorExperimentalAPI
fun Route.users(api: Controller) {
    get(ApiPath.USER) {
        val httpRequest = wrapAsRequest(ApiPath.ARG_ID, call.principal<UserPrincipal>()?.id ?: emptyString())
        dispatch(api.getUser(httpRequest))
    }
}

fun Route.exercises(api: Controller) {
    get(ApiPath.EXERCISES) {
        dispatch(api.getExercises())
    }

    get(ApiPath.EXERCISES_PAGE) {
        call.respondText(
            this::class.java.classLoader.getResource("index.html")!!.readText(),
            ContentType.Text.Html
        )
    }
}

fun Route.categories(api: Controller) {
    get(ApiPath.CATEGORIES) {
        dispatch(api.getCategories())
    }
}

fun Route.equipment(api: Controller) {
    get(ApiPath.EQUIPMENT) {
        dispatch(api.getEquipment())
    }
}

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

fun wrapAsRequest(key: String, arg: Any): HttpRequest<Unit> = wrapAsRequest(Unit, key, arg)

fun <T> wrapAsRequest(body: T, key: String, arg: Any): HttpRequest<T> = HttpRequest(body, mapOf(key to arg.toString()))

private inline fun <reified T : Any> Route.getByIdEndpoint(path: String, apiFunRef: KFunction1<HttpRequest<Unit>, HttpResponse<T>>) {
    get(path) {
        val httpRequest = HttpRequest(
            data = Unit,
            parameters = mapOf(
                ApiPath.ARG_ID to call.parameters[ApiPath.ARG_ID],
                ApiPath.ARG_USER_ID to (call.principal<UserPrincipal>()?.id ?: emptyString()).toString()
            )
        )
        dispatch(apiFunRef(httpRequest))
    }
}

private fun <T : Any> Route.getAllEndpoint(
    path: String,
    apiFunRef: KFunction1<HttpRequest<Unit>, HttpResponse<List<T>>>
) {
    get(path) {
        val httpRequest = HttpRequest(
            data = Unit,
            parameters = mapOf(
                ApiPath.ARG_USER_ID to (call.principal<UserPrincipal>()?.id ?: emptyString()).toString()
            )
        )
        dispatch(apiFunRef(httpRequest))
    }
}

private inline fun <reified T : Any> Route.deleteByIdEndpoint(
    path: String,
    apiFunRef: KFunction1<HttpRequest<Unit>, HttpResponse<T>>
) {
    delete(path) {
        val httpRequest = HttpRequest(
            Unit,
            mapOf(
                ApiPath.ARG_USER_ID to call.principal<UserPrincipal>()?.id?.toString(),
                ApiPath.ARG_ID to call.parameters[ApiPath.ARG_ID],
            )
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
        val taskId = call.parameters[TodoApiPath.ARG_ID]
        val httpRequest = HttpRequest(
            dto,
            mapOf(
                ApiPath.ARG_USER_ID to call.principal<UserPrincipal>()?.id?.toString(),
                ApiPath.ARG_ID to taskId,
            )
        )
        dispatch(apiFunRef(httpRequest))
    }
}