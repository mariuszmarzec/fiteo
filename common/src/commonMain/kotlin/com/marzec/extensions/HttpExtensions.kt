package com.marzec.extensions

import com.marzec.Api
import com.marzec.exceptions.HttpException
import com.marzec.exceptions.HttpStatus
import com.marzec.fiteo.model.dto.ErrorDto
import com.marzec.fiteo.model.http.HttpRequest
import com.marzec.fiteo.model.http.HttpResponse

fun <T> serviceCall(call: () -> T): HttpResponse<T> {
        return try {
            HttpResponse.Success(call())
        } catch (e: Exception) {
            when (e) {
                is HttpException -> HttpResponse.Error(ErrorDto(e.message.orEmpty()), e.httpStatus)
                else -> HttpResponse.Error(ErrorDto(e.message.orEmpty()), HttpStatus.INTERNAL_SERVER_ERROR)
            }
        }
    }

fun <REQUEST, RESPONSE> HttpRequest<REQUEST>.serviceCall(
    constraints: List<(HttpRequest<REQUEST>.() -> Unit)> = emptyList(),
    call: HttpRequest<REQUEST>.() -> RESPONSE
): HttpResponse<RESPONSE> = serviceCall<RESPONSE> {
    constraints.forEach { constraint -> constraint() }
    this.call()
}

fun <REQUEST> constraint(
    check: HttpRequest<REQUEST>.() -> Boolean,
    exception: (HttpRequest<REQUEST>.() -> Exception)? = null
): HttpRequest<REQUEST>.() -> Unit = {
    if (!check()) {
        throw exception?.invoke(this) ?: HttpException("Bad request", HttpStatus.BAD_REQUEST)
    }
}

fun <REQUEST, RESPONSE> HttpRequest<REQUEST>.serviceCall(
    constraint: HttpRequest<REQUEST>.() -> Unit,
    call: HttpRequest<REQUEST>.() -> RESPONSE
): HttpResponse<RESPONSE> = serviceCall(constraint.listOf(), call)

fun <T> HttpRequest<T>.userIdOrThrow() = (sessions[Api.Args.ARG_USER_ID])?.toIntOrNull()
        ?: throw HttpException("Session id: ${Api.Args.ARG_USER_ID} is not integer", HttpStatus.BAD_REQUEST)

fun <T> HttpRequest<T>.getIntOrThrow(key: String) = parameters[key]?.toIntOrNull()
        ?: throw HttpException("Argument $key is empty or not integer", HttpStatus.BAD_REQUEST)
