package com.marzec

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.marzec.Api.Auth
import com.marzec.Api.Headers
import com.marzec.cheatday.CheatDayController
import com.marzec.cheatday.cheatDayApi
import com.marzec.common.createHttpRequest
import com.marzec.common.dispatch
import com.marzec.common.getBySessionEndpoint
import com.marzec.common.postEndpoint
import com.marzec.core.CurrentTimeUtil
import com.marzec.core.currentMillis
import com.marzec.database.DbSettings
import com.marzec.database.UserPrincipal
import com.marzec.database.toPrincipal
import com.marzec.di.Di
import com.marzec.di.MainModule
import com.marzec.di.diModules
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
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.auth.*
import io.ktor.server.auth.authenticate
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
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.ktor.plugin.Koin
import org.koin.ktor.plugin.KoinApplicationStarted
import org.koin.logger.slf4jLogger
import org.slf4j.event.Level
import java.util.*
import javax.crypto.spec.SecretKeySpec

private const val PRIORITY = 10.0
private const val MINIMUM_SIZE: Long = 1024

fun main(args: Array<String>) {
    CurrentTimeUtil.init(TimeZone.getTimeZone("GMT+2"))
    EngineMain.main(args)
}

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

            authorizationApi(api, di)
            cheatDayApi(di, cheatDayApi)
            todoApi(di, todoController)
            fiteoApi(di, api)
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
        modules(diModules)
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

        bearer(Auth.BEARER) {
            authenticate { tokenCredential ->
                val secret = "secret"
                val issuer = "http://0.0.0.0:8080/"
                val audience = "http://0.0.0.0:8080/users"

                val jwt = JWT.require(Algorithm.HMAC256(secret))
                    .withAudience(audience)
                    .withIssuer(issuer)
                    .build()
                    .verify(tokenCredential.token)

                val user = di.userRepository.getUser(jwt.getClaim("mail").asString())
                UserPrincipal(user.id, user.email)
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
        user(api)
        logout()
    }

    if (di.authToken == Auth.NAME) {
        loginBearer(api)
        authenticate(Auth.BEARER) {
            users(api)
        }
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

private fun Route.loginBearer(api: Controller) {
    post(ApiPath.LOGIN_BEARER) {
        val loginRequestDto = call.receiveOrNull<LoginRequestDto>()
        val httpResponse = api.postLogin(HttpRequest(loginRequestDto))
        if (httpResponse is HttpResponse.Success<UserDto>) {
            val secret = "secret"
            val issuer = "http://0.0.0.0:8080/"
            val audience = "http://0.0.0.0:8080/users"
            val token = JWT.create()
                .withAudience(audience)
                .withIssuer(issuer)
                .withClaim("mail", httpResponse.data.email)
                .withExpiresAt(Date(System.currentTimeMillis() + 60000))
                .sign(Algorithm.HMAC256(secret))
            call.response.headers.append(Headers.AUTHORIZATION, "Bearer $token")
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

fun Route.user(api: Controller) = getBySessionEndpoint(ApiPath.USER, api::getUser)

fun Route.users(api: Controller) = getBySessionEndpoint(ApiPath.USERS, api::getUsers)
