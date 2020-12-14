package com.marzec.model.http

import com.marzec.model.domain.Session

data class HttpRequest<T>(
        val data: T,
        val parameters: Map<String, String?> = mapOf(),
        val sessions: Map<String, Session> = mapOf()
)