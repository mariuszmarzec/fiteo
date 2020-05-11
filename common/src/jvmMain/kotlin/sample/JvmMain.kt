package sample

import com.marzec.Constants.PATH_EXERCISES
import com.marzec.di.DI
import com.marzec.model.dto.CategoryFileDto
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.serialization.json
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main() {

    val api = DI.provideApi()

    DI.provideDataSource().loadData()

    embeddedServer(Netty, 8080) {

        install(ContentNegotiation) {
            json(
                    contentType = ContentType.Application.Json,
                    json = DI.provideJson()
            )
        }

        routing {
            get(PATH_EXERCISES) {
                call.respond(api.getExercises())
            }
        }
    }.start(wait = true)
}