package com.marzec

import com.marzec.api.Controller
import com.marzec.database.DbSettings
import com.marzec.database.UserEntity
import com.marzec.di.DI
import com.marzec.fiteo.BuildKonfig
import com.marzec.model.dto.LoginRequestDto
import com.marzec.model.http.HttpRequest
import com.marzec.model.http.HttpResponse
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.ApplicationStarted
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveOrNull
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.serialization.json
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.sessions.SessionStorageMemory
import io.ktor.sessions.Sessions
import io.ktor.sessions.header
import io.ktor.sessions.sessions
import io.ktor.util.pipeline.PipelineContext
import org.jetbrains.exposed.sql.Schema
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

data class UserSession(
        val id: String,
        val userId: String,
        val authToken: String
)

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

        install(ContentNegotiation) {
            json(
                    contentType = ContentType.Application.Json,
                    json = DI.provideJson()
            )
        }

        install(Sessions) {
            header<UserSession>(Headers.AUTHORIZATION, SessionStorageMemory()) {
                identity {
                    UUID.randomUUID().toString()
                }
            }
        }

        routing {
            login(api)
            exercises(api)
            categories(api)
            equipment(api)
            trainings(api)
        }
    }.start(wait = true)
}

fun Route.login(api: Controller) {
    post(ApiPath.LOGIN) {
        val loginRequestDto = call.receiveOrNull<LoginRequestDto>()
        val httpResponse = api.postLogin(HttpRequest(loginRequestDto))
        dispatch(httpResponse)
    }
}

fun Route.exercises(api: Controller) {
    get(ApiPath.EXERCISES) {
        dispatch(api.getExercises())
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

private suspend fun <T: Any> PipelineContext<Unit, ApplicationCall>.dispatch(response: HttpResponse<T>) {
    when (response) {
        is HttpResponse.Success -> call.respond(response.data)
        is HttpResponse.Error -> call.respond(HttpStatusCode.fromValue(response.httpStatusCode), response.data)
    }
}