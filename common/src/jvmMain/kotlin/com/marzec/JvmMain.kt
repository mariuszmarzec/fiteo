package com.marzec

import com.marzec.Constants.PATH_CATEGORIES
import com.marzec.Constants.PATH_EQUIPMENT
import com.marzec.Constants.PATH_EXERCISES
import com.marzec.Constants.PATH_TRAININGS
import com.marzec.Constants.PATH_TRAINING_TEMPLATES
import com.marzec.api.Controller
import com.marzec.database.DbSettings
import com.marzec.database.UserEntity
import com.marzec.di.DI
import com.marzec.fiteo.BuildKonfig
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.jetbrains.exposed.sql.Schema
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction

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
            categories(api)
            equipment(api)
            trainings(api)
        }
    }.start(wait = true)
}

fun Route.exercises(api: Controller) {
    get(PATH_EXERCISES) {
        call.respond(api.getExercises().data)
    }
}

fun Route.categories(api: Controller) {
    get(PATH_CATEGORIES) {
        call.respond(api.getCategories().data)
    }
}

fun Route.equipment(api: Controller) {
    get(PATH_EQUIPMENT) {
        call.respond(api.getEquipment().data)
    }
}

fun Route.trainings(api: Controller) {
    get(PATH_TRAININGS) {
        call.respond(api.getTrainings().data)
    }
    get(PATH_TRAINING_TEMPLATES) {
        call.respond(api.getTrainingTemplates().data)
    }
}