package com.marzec

import com.marzec.Api.Auth
import com.marzec.Api.Headers
import com.marzec.cheatday.CheatDayController
import com.marzec.cheatday.cheatDayApi
import com.marzec.common.createHttpRequest
import com.marzec.common.dispatch
import com.marzec.common.getBySessionEndpoint
import com.marzec.common.postEndpoint
import com.marzec.core.currentMillis
import com.marzec.database.DbSettings
import com.marzec.database.toPrincipal
import com.marzec.di.Di
import com.marzec.di.MainModule
import com.marzec.fiteo.ApiPath
import com.marzec.fiteo.api.Controller
import com.marzec.fiteo.fiteoApi
import com.marzec.fiteo.model.domain.TestUserSession
import com.marzec.fiteo.model.domain.UserSession
import com.marzec.fiteo.model.dto.LoginRequestDto
import com.marzec.fiteo.model.dto.UserDto
import com.marzec.fiteo.model.http.HttpRequest
import com.marzec.fiteo.model.http.HttpResponse
import com.marzec.sessions.DatabaseSessionStorage
import com.marzec.todo.ToDoApiController
import com.marzec.todo.schedule.runTodoSchedulerDispatcher
import com.marzec.todo.todoApi
import com.marzec.trader.TraderApiController
import com.marzec.trader.traderApi
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.UnauthorizedResponse
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.session
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.request.receiveOrNull
import io.ktor.server.request.uri
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.server.http.content.resource
import io.ktor.server.http.content.static
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.compression.Compression
import io.ktor.server.plugins.compression.deflate
import io.ktor.server.plugins.compression.gzip
import io.ktor.server.plugins.compression.minimumSize
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.defaultheaders.DefaultHeaders
import io.ktor.server.sessions.SessionTransportTransformerMessageAuthentication
import io.ktor.server.sessions.Sessions
import io.ktor.server.sessions.clear
import io.ktor.server.sessions.header
import io.ktor.server.sessions.sessions
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.ktor.plugin.Koin
import org.koin.ktor.plugin.KoinApplicationStarted
import org.koin.logger.slf4jLogger
import org.slf4j.event.Level
import javax.crypto.spec.SecretKeySpec

private const val PRIORITY = 10.0
private const val MINIMUM_SIZE: Long = 1024

fun main(args: Array<String>) = EngineMain.main(args)

@Suppress("unused")
fun Application.module() {
    val di = Di(DbSettings.database, Auth.NAME)
    val testDi = Di(DbSettings.testDatabase, Auth.TEST)

    environment.monitor.subscribe(KoinApplicationStarted) {
        di.dataSource.loadData()
        testDi.dataSource.loadData()

        clearSessionsInPeriod(di, testDi)
        runTodoSchedulerDispatcher(di, testDi)
    }

    configuration(di)
    sessions(di, testDi)

    routing {

        static {
            resource("/", "index.html")
            resource("/fiteo.js", "fiteo.js")
        }

        apiSetup(testDi, di) { di ->
            val api: Controller = di.api
            val cheatDayApi: CheatDayController = di.cheatDayController
            val todoController: ToDoApiController = di.todoController
            val traderApiController: TraderApiController = di.traderApiController

            authorizationApi(api, di)
            cheatDayApi(di, cheatDayApi)
            todoApi(di, todoController)
            fiteoApi(di, api)
            traderApi(di, traderApiController)
        }
    }
}

private fun clearSessionsInPeriod(di: Di, testDi: Di) {
    val repository = di.cachedSessionsRepository
    val testRepository = testDi.cachedSessionsRepository
    val period = di.sessionExpirationTime
    GlobalScope.launch {
        while (true) {
            repository.clearOldSessions()
            testRepository.clearOldSessions()
            delay(period)
        }
    }
}

fun Application.configuration(di: Di) {
    install(CallLogging) {
        level = Level.INFO
    }

    install(Koin) {
        slf4jLogger(level = org.koin.core.logger.Level.ERROR)
        modules(MainModule)
    }

    install(DefaultHeaders)

    install(Compression) {
        gzip()
        deflate {
            priority = PRIORITY
            minimumSize(MINIMUM_SIZE)
        }
    }

    install(CORS) {
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Delete)
        anyHost()
    }

    install(ContentNegotiation) {
        json(
            contentType = ContentType.Application.Json,
            json = di.json
        )
    }
}

private fun Application.sessions(di: Di, testDi: Di) {
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
}

private fun Route.apiSetup(testDi: Di, prodDi: Di, setup: Route.(di: Di) -> Unit) {

    route(ApiPath.TEST_API_PREFIX) {
        setup(testDi)
    }
    setup(prodDi)
}

private fun Route.authorizationApi(api: Controller, di: Di) {
    login(api)
    register(api)

    authenticate(di.authToken) {
        users(api)
        logout()
    }
}

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
