package com.marzec.model.http

data class HttpResponse<T>(val data: T, val httpStatusCode: Int = 200)