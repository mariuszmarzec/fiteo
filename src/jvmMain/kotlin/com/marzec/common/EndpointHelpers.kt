package com.marzec.common

import com.marzec.Api
import com.marzec.database.UserPrincipal
import com.marzec.fiteo.model.dto.ErrorDto
import com.marzec.fiteo.model.http.HttpRequest
import com.marzec.fiteo.model.http.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.RoutingContext

suspend fun <T : Any> RoutingContext.respond(response: HttpResponse<T>) {
    response.headersOrEmpty().forEach { (header, value) ->
        call.response.headers.append(header, value)
    }

    val errorData = response.errorDataOrNull()
    if (errorData != null) {
        call.respond(
            HttpStatusCode.fromValue(response.httpStatusCodeValue()),
            errorData
        )
    } else {
        call.respond(response.successDataOrNull()!!)
    }
}

private fun HttpResponse<*>.headersOrEmpty(): Map<String, String> =
    when (this) {
        is HttpResponse.Success -> headers
        is HttpResponse.Error -> emptyMap()
    }

private fun HttpResponse<*>.httpStatusCodeValue(): Int =
    when (this) {
        is HttpResponse.Success -> httpStatusCode
        is HttpResponse.Error -> httpStatusCode
    }

private fun HttpResponse<*>.errorDataOrNull(): ErrorDto? =
    when (this) {
        is HttpResponse.Success -> null
        is HttpResponse.Error -> data
    }

private fun <T : Any> HttpResponse<T>.successDataOrNull(): T? =
    when (this) {
        is HttpResponse.Success -> data
        is HttpResponse.Error -> null
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

/** Keeps request decoding outside Ktor/OpenAPI route-builder lambdas. */
suspend inline fun <reified REQUEST : Any> RoutingContext.receiveHttpRequest(): HttpRequest<REQUEST> =
    createHttpRequest(call.receive<REQUEST>())
