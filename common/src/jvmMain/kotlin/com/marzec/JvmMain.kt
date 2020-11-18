package com.marzec

import com.marzec.api.Controller
import com.marzec.database.CategoryEntity
import com.marzec.database.CategoryTable
import com.marzec.database.DbSettings
import com.marzec.database.UserEntity
import com.marzec.database.UserPrincipal
import com.marzec.database.toPrincipal
import com.marzec.di.DI
import com.marzec.extensions.emptyString
import com.marzec.fiteo.BuildKonfig
import com.marzec.html.renderExercises
import com.marzec.model.domain.UserSession
import com.marzec.model.dto.LoginRequestDto
import com.marzec.model.dto.RegisterRequestDto
import com.marzec.model.dto.UserDto
import com.marzec.model.http.HttpRequest
import com.marzec.model.http.HttpResponse
import com.marzec.sessions.DatabaseSessionStorage
import com.marzec.todo.database.TaskToSubtasksTable
import com.marzec.todo.database.TasksTable
import com.marzec.todo.database.ToDoListTable
import com.marzec.todo.database.ToDoListsToTasksTable
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
import io.ktor.features.Compression
import io.ktor.features.ContentNegotiation
import io.ktor.features.deflate
import io.ktor.features.gzip
import io.ktor.features.minimumSize
import io.ktor.html.respondHtml
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.request.receiveOrNull
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
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
import org.jetbrains.exposed.sql.Schema
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import java.lang.System.currentTimeMillis
import javax.crypto.spec.SecretKeySpec

@KtorExperimentalAPI
fun main() {
    val api = DI.provideApi()

    val onServerStart: (Application) -> Unit = {
        DI.provideDataSource().loadData()
    }

    println("Database version: ${DbSettings.database.version}")

    transaction {
        addLogger(StdOutSqlLogger)

        SchemaUtils.setSchema(Schema(BuildKonfig.DB_DATABASE))

        val users = UserEntity.all()
        println(users.toList())
    }

    embeddedServer(Netty, 5000) {

        environment.monitor.subscribe(ApplicationStarted, onServerStart)

        install(Compression) {
            gzip()
            deflate {
                priority = 10.0
                minimumSize(1024)
            }
        }
        
        install(ContentNegotiation) {
            json(
                    contentType = ContentType.Application.Json,
                    json = DI.provideJson()
            )
        }

        install(Sessions) {
            header<UserSession>(Headers.AUTHORIZATION, DatabaseSessionStorage(DI.provideCachedSessionsRepository())) {
                transform(SessionTransportTransformerMessageAuthentication(SecretKeySpec("key".toByteArray(), "AES")))
            }
        }

        install(Authentication) {
            session<UserSession>(Auth.NAME) {
                challenge {
                    call.respond(UnauthorizedResponse())
                }
                validate { session: UserSession ->
                    when (val httpResponse = api.getUser(wrapAsRequest(ApiPath.ARG_ID, session.userId))) {
                        is HttpResponse.Success -> httpResponse.data.toPrincipal()
                        is HttpResponse.Error -> null
                    }
                }
            }
        }

        routing {

            get("/") {
                call.respondText(
                        this::class.java.classLoader.getResource("index.html")!!.readText(),
                        ContentType.Text.Html
                )
            }

            login(api)
            register(api)
            authenticate(Auth.NAME) {
                users(api)
                logout()
            }
            equipment(api)
            exercises(api)
            categories(api)
            trainings(api)
        }
    }.start(wait = true)
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
        call.respondHtml {
            renderExercises(api.getExercises())
        }
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
    get(ApiPath.TRAINING_TEMPLATES) {
        dispatch(api.getTrainingTemplates())
    }
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

fun wrapAsRequest(key: String, arg: Any): HttpRequest<Unit> = HttpRequest(Unit, mapOf(key to arg.toString()))