package com.marzec.model.http

import com.marzec.model.dto.ErrorDto

sealed class HttpResponse<T> {

    data class Success<T>(val data: T, val httpStatusCode: Int = 200, val headers: Map<String, String> = emptyMap()): HttpResponse<T>()
    data class Error<T>(val data: ErrorDto, val httpStatusCode: Int = 400): HttpResponse<T>()
}