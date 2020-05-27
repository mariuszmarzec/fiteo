package com.marzec

import com.marzec.Constants.PATH_EXERCISES
import com.marzec.api.Controller
import com.marzec.di.DI
import io.ktor.application.Application
import io.ktor.application.ApplicationStarted
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.serialization.json
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main() {
    val api = DI.provideApi()

    val onServerStart: (Application) -> Unit = {
        DI.provideDataSource().loadData()
    }
    embeddedServer(Netty, 8080) {

        environment.monitor.subscribe(ApplicationStarted, onServerStart)

        install(ContentNegotiation) {
            json(
                    contentType = ContentType.Application.Json,
                    json = DI.provideJson()
            )
        }

        routing {
            exercises(api)
        }
    }.start(wait = true)
}

fun Route.exercises(api: Controller) {
    get(PATH_EXERCISES) {
        call.respond(api.getExercises().data)
    }
}