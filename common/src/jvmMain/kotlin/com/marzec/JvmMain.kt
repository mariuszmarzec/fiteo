package com.marzec

import com.marzec.cheatday.ApiPath as CheatDayApiPath
import com.marzec.todo.ApiPath as TodoApiPath
import com.marzec.api.Controller
import com.marzec.cheatday.CheatDayController
import com.marzec.cheatday.dto.PutWeightDto
import com.marzec.cheatday.dto.WeightDto
import com.marzec.database.DbSettings
import com.marzec.database.UserEntity
import com.marzec.database.UserPrincipal
import com.marzec.database.dbCall
import com.marzec.database.toPrincipal
import com.marzec.di.Di
import com.marzec.extensions.emptyString
import com.marzec.model.domain.CreateTrainingTemplateDto
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
import com.marzec.todo.model.UpdateTaskDto
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.ApplicationStarted
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.UnauthorizedResponse
import io.ktor.auth.authenticate
import io.ktor.auth.principal
import io.ktor.auth.session
import io.ktor.features.CORS
import io.ktor.features.Compression
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.features.deflate
import io.ktor.features.gzip
import io.ktor.features.minimumSize
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.request.receive
import io.ktor.request.receiveOrNull
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
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.sessions.SessionTransportTransformerMessageAuthentication
import io.ktor.sessions.Sessions
import io.ktor.sessions.clear
import io.ktor.sessions.header
import io.ktor.sessions.sessions
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.pipeline.PipelineContext
import java.lang.System.currentTimeMillis
import javax.crypto.spec.SecretKeySpec
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger

@KtorExperimentalAPI
fun main() {
    val di = Di(DbSettings.database)
    val testDi = Di(DbSettings.testDatabase)

    val onServerStart: (Application) -> Unit = {
        di.provideDataSource().loadData()
    }

    println("Database version: ${DbSettings.database.version}")

    DbSettings.database.dbCall {
        addLogger(StdOutSqlLogger)

        val users = UserEntity.all()
        println(users.toList())
    }

    embeddedServer(Netty, 5000) {

        environment.monitor.subscribe(ApplicationStarted, onServerStart)

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
                    json = di.provideJson()
            )
        }

        install(Sessions) {
            header<UserSession>(Headers.AUTHORIZATION, DatabaseSessionStorage(di.provideCachedSessionsRepository())) {
                transform(SessionTransportTransformerMessageAuthentication(SecretKeySpec("key".toByteArray(), "AES")))
            }
        }

        install(Authentication) {
            session<UserSession>(Auth.NAME) {
                challenge {
                    call.respond(UnauthorizedResponse())
                }
                validate { session: UserSession ->
                    when (val httpResponse = di.provideApi().getUser(wrapAsRequest(ApiPath.ARG_ID, session.userId))) {
                        is HttpResponse.Success -> httpResponse.data.toPrincipal()
                        is HttpResponse.Error -> null
                    }
                }
            }
        }

        routing {

            static("/") {
                resources("")
            }

            apiSetup(testDi, di) {
                val api: Controller = di.provideApi()
                val cheatDayApi: CheatDayController = di.provideCheatDayController()
                val todoController: ToDoApiController = di.provideTodoController()

                login(api)
                register(api)
                authenticate(Auth.NAME) {
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
                    users(api)
                    logout()
                }
                equipment(api)
                exercises(api)
                categories(api)
                trainings(api)
            }
        }
    }.start(wait = true)
}

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

fun Route.removeTemplate(api: Controller) {
    delete(ApiPath.DELETE_TRAINING_TEMPLATES) {
        val weightId = call.parameters[TodoApiPath.ARG_ID]
        val httpRequest = HttpRequest(
                Unit,
                mapOf(
                        ApiPath.ARG_USER_ID to call.principal<UserPrincipal>()?.id?.toString(),
                        ApiPath.ARG_ID to weightId,
                )
        )
        dispatch(api.removeTrainingTemplate(httpRequest))
    }
}

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

fun Route.updateTask(api: ToDoApiController) {
    patch(TodoApiPath.UPDATE_TASK) {
        val createDto = call.receive<UpdateTaskDto>()
        val taskId = call.parameters[TodoApiPath.ARG_ID]
        val httpRequest = HttpRequest(
                createDto,
                mapOf(
                        ApiPath.ARG_USER_ID to call.principal<UserPrincipal>()?.id?.toString(),
                        ApiPath.ARG_ID to taskId,
                )
        )
        dispatch(api.updateTask(httpRequest))
    }
}

fun Route.removeTask(api: ToDoApiController) {
    delete(TodoApiPath.DELETE_TASK) {
        val taskId = call.parameters[TodoApiPath.ARG_ID]
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
            call.sessions.set(Headers.AUTHORIZATION, UserSession(httpResponse.data.id, currentTimeMillis()))
        }
        dispatch(httpResponse)
    }
}

fun Route.logout() {
    get(ApiPath.LOGOUT) {
        call.sessions.clear<UserSession>()
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

fun Route.trainings(api: Controller) {
    get(ApiPath.TRAININGS) {
        dispatch(api.getTrainings())
    }
}

fun Route.apiSetup(testDi: Di, prodDi: Di, setup: Route.(di: Di) -> Unit) {

    route(ApiPath.TEST_API_PREFIX) {
        setup(testDi)
    }
    setup(prodDi)
}

private suspend fun <T : Any> PipelineContext<Unit, ApplicationCall>.dispatch(response: HttpResponse<T>) {
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