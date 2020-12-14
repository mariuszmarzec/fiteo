package com.marzec.extensions

import com.marzec.ApiPath
import com.marzec.exceptions.HttpException
import com.marzec.model.dto.ErrorDto
import com.marzec.model.http.HttpRequest
import com.marzec.model.http.HttpResponse

fun <T> serviceCall(call: () -> T): HttpResponse<T> {
        return try {
            HttpResponse.Success(call())
        } catch (e: Exception) {
            when (e) {
                is HttpException -> HttpResponse.Error(ErrorDto(e.message.orEmpty()), e.httpStatus)
                else -> HttpResponse.Error(ErrorDto(e.message.orEmpty()), 500)
            }
        }
    }

fun <T> HttpRequest<T>.userIdOrThrow() = (parameters[ApiPath.ARG_USER_ID] ?: parameters[ApiPath.ARG_ID])?.toIntOrNull()
        ?: throw HttpException("Argument ${ApiPath.ARG_ID} is not integer", 400)

fun <T> HttpRequest<T>.getIntOrThrow(key: String) = parameters[key]?.toIntOrNull()
        ?: throw HttpException("Argument $key is empty or not integer", 400)