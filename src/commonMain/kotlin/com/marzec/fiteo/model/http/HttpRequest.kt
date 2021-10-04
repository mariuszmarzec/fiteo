package com.marzec.fiteo.model.http

data class HttpRequest<T>(
        val data: T,
        val parameters: Map<String, String?> = emptyMap(),
        val sessions: Map<String, String> = emptyMap()
)
