package com.marzec.model.http

data class HttpRequest<T>(val data: T, val parameters: Map<String, String> = mapOf())