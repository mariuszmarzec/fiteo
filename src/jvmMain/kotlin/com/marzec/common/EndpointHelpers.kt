package com.marzec.common

import com.marzec.Api
import com.marzec.database.UserPrincipal
import com.marzec.fiteo.model.http.HttpRequest
import com.marzec.fiteo.model.http.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.RoutingContext

suspend inline fun <reified T : Any> RoutingContext.respond(response: HttpResponse<T>) {
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

fun <T> RoutingContext.createHttpRequest(data: T): HttpRequest<T> = HttpRequest(
    data = data,
    parameters = mapOf(
        Api.Args.ARG_ID to call.parameters[Api.Args.ARG_ID],
    ),
    sessions = mapOf(Api.Args.ARG_USER_ID to call.principal<UserPrincipal>()?.id.toString()),
    queries = call.request.queryParameters.entries().associate { it.key to it.value.toList() }
)

fun createHttpRequest(userId: Int?): HttpRequest<Unit> = HttpRequest(
    data = Unit,
    parameters = emptyMap(),
    sessions = mapOf(Api.Args.ARG_USER_ID to userId.toString()),
)
