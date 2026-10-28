package com.marzec.mcp

import com.marzec.exceptions.HttpException
import com.marzec.fiteo.services.PersonalAccessTokenService
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.request.path
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.mcpStatelessStreamableHttp
import io.modelcontextprotocol.kotlin.sdk.types.Implementation
import io.modelcontextprotocol.kotlin.sdk.types.ServerCapabilities

internal const val MCP_ENDPOINT_PATH = "/mcp"

internal fun createMcpServer(): Server = Server(
    serverInfo = Implementation(
        name = "fiteo",
        version = "1.0.0",
    ),
    options = ServerOptions(
        capabilities = ServerCapabilities(
            tools = ServerCapabilities.Tools(listChanged = false),
        ),
    ),
)

/**
 * Registers PAT authentication before the SDK's Streamable HTTP handler.
 *
 * Only the exact MCP endpoint is protected here. Existing REST routes keep their current
 * authentication configuration. Authentication failures return a neutral 401 without a body
 * or logging the supplied credential.
 */
internal fun Application.configureMcpEndpoint(
    personalAccessTokenService: () -> PersonalAccessTokenService,
) {
    intercept(ApplicationCallPipeline.Plugins) {
        if (call.request.path() == MCP_ENDPOINT_PATH && !call.hasValidMcpPat(personalAccessTokenService)) {
            call.response.header(HttpHeaders.WWWAuthenticate, "Bearer")
            call.response.header(HttpHeaders.CacheControl, "no-store")
            call.respond(HttpStatusCode.Unauthorized)
            finish()
        }
    }

    mcpStatelessStreamableHttp(
        path = MCP_ENDPOINT_PATH,
        enableDnsRebindingProtection = false,
    ) {
        createMcpServer()
    }
}

private fun io.ktor.server.application.ApplicationCall.hasValidMcpPat(
    personalAccessTokenService: () -> PersonalAccessTokenService,
): Boolean {
    val token = request.headers[HttpHeaders.Authorization].toBearerToken() ?: return false
    return try {
        personalAccessTokenService().verify(token)
        true
    } catch (_: HttpException) {
        false
    }
}

private fun String?.toBearerToken(): String? {
    val value = this ?: return null
    val prefix = "Bearer "
    if (!value.startsWith(prefix, ignoreCase = true)) return null

    val token = value.substring(prefix.length)
    if (token.isBlank() || token.any(Char::isWhitespace)) return null
    return token
}
