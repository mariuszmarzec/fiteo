package com.marzec.mcp

import com.marzec.Api
import com.marzec.exceptions.HttpException
import com.marzec.exceptions.HttpStatus
import com.marzec.fiteo.model.domain.User
import com.marzec.fiteo.model.http.HttpRequest
import io.ktor.util.AttributeKey
import kotlinx.serialization.json.JsonObject

/**
 * Request-scoped bridge between a verified PAT owner and the existing user-scoped controllers.
 *
 * Instances are created only from the owner returned by PersonalAccessTokenService.verify().
 * The caller identity is written to HttpRequest.sessions rather than accepted from tool input.
 */
internal class McpRequestContext private constructor(
    private val ownerUserId: Int,
) {
    fun <T> createRequest(
        data: T,
        arguments: JsonObject,
        parameters: Map<String, String?> = emptyMap(),
        queries: Map<String, List<String>> = emptyMap(),
    ): HttpRequest<T> {
        rejectCallerIdentityOverride(arguments, parameters, queries)

        return HttpRequest(
            data = data,
            parameters = parameters,
            sessions = mapOf(Api.Args.ARG_USER_ID to ownerUserId.toString()),
            queries = queries,
        )
    }

    private fun rejectCallerIdentityOverride(
        arguments: JsonObject,
        parameters: Map<String, String?>,
        queries: Map<String, List<String>>,
    ) {
        if (Api.Args.ARG_USER_ID in arguments ||
            Api.Args.ARG_USER_ID in parameters ||
            Api.Args.ARG_USER_ID in queries
        ) {
            throw HttpException(
                "MCP tool arguments must not specify the caller userId",
                HttpStatus.BAD_REQUEST,
            )
        }
    }

    companion object {
        fun fromVerifiedPatOwner(owner: User): McpRequestContext =
            McpRequestContext(ownerUserId = owner.id)
    }
}

internal val McpRequestContextAttributeKey = AttributeKey<McpRequestContext>("McpRequestContext")
