package com.marzec.common

import com.marzec.Api
import com.marzec.database.UserPrincipal
import com.marzec.extensions.emptyString
import com.marzec.fiteo.model.http.HttpRequest
import com.marzec.fiteo.model.http.HttpResponse
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.auth.principal
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.util.pipeline.PipelineContext
import kotlin.reflect.KFunction1

suspend inline fun <reified T : Any> PipelineContext<Unit, ApplicationCall>.dispatch(response: HttpResponse<T>) {
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

inline fun <reified T : Any> Route.getByIdEndpoint(
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

inline fun <reified T : Any> Route.getAllEndpoint(
    path: String,
    apiFunRef: KFunction1<HttpRequest<Unit>, HttpResponse<T>>
) {
    get(path) {
        (call.principal<UserPrincipal>()?.id ?: emptyString()).toString()
        val httpRequest = createHttpRequest(call.principal<UserPrincipal>()?.id)
        dispatch(apiFunRef(httpRequest))
    }
}

inline fun <reified T : Any> Route.deleteByIdEndpoint(
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

inline fun <reified REQUEST : Any, reified RESPONSE : Any> Route.updateByIdEndpoint(
    path: String,
    apiFunRef: KFunction1<HttpRequest<REQUEST>, HttpResponse<RESPONSE>>
) {
    patch(path) {
        val dto = call.receive<REQUEST>()
        val id = call.parameters[Api.Args.ARG_ID]
        val httpRequest = HttpRequest(
            data = dto,
            parameters = mapOf(Api.Args.ARG_ID to id),
            sessions = mapOf(Api.Args.ARG_USER_ID to call.principal<UserPrincipal>()?.id.toString())
        )
        dispatch(apiFunRef(httpRequest))
    }
}

inline fun <reified REQUEST : Any, reified RESPONSE : Any> Route.postEndpoint(
    path: String,
    apiFunRef: KFunction1<HttpRequest<REQUEST>, HttpResponse<RESPONSE>>
) {
    post(path) {
        val dto = call.receive<REQUEST>()
        val id = call.parameters[Api.Args.ARG_ID]
        val httpRequest = HttpRequest(
            data = dto,
            parameters = mapOf(
                Api.Args.ARG_ID to id,
            ),
            sessions = mapOf(Api.Args.ARG_USER_ID to call.principal<UserPrincipal>()?.id.toString())
        )
        dispatch(apiFunRef(httpRequest))
    }
}

inline fun <reified T : Any> Route.getBySessionEndpoint(
    path: String,
    apiFunRef: KFunction1<HttpRequest<Unit>, HttpResponse<T>>
) {
    get(path) {
        val id = call.principal<UserPrincipal>()?.id
        val httpRequest = createHttpRequest(id)
        dispatch(apiFunRef(httpRequest))
    }
}

fun createHttpRequest(userId: Int?): HttpRequest<Unit> = HttpRequest(
    data = Unit,
    parameters = emptyMap(),
    sessions = mapOf(Api.Args.ARG_USER_ID to userId.toString())
)
