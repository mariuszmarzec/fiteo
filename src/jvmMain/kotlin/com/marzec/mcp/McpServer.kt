package com.marzec.mcp

import com.marzec.exceptions.HttpException
import com.marzec.fiteo.model.domain.User
import com.marzec.fiteo.services.PersonalAccessTokenService
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.call
import io.ktor.server.request.path
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.mcpStatelessStreamableHttp
import io.modelcontextprotocol.kotlin.sdk.types.Implementation
import io.modelcontextprotocol.kotlin.sdk.types.ServerCapabilities

internal const val MCP_ENDPOINT_PATH = "/mcp"

@Suppress("UNUSED_PARAMETER")
internal fun createMcpServer(requestContext: McpRequestContext): Server = Server(
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
 * Registers PAT authentication before the SDK's Streamable HTTP handler and retains the
 * verified owner's request context only on the current Ktor call.
 */
internal fun Application.configureMcpEndpoint(
    personalAccessTokenService: () -> PersonalAccessTokenService,
) {
    intercept(ApplicationCallPipeline.Plugins) {
        if (call.request.path() == MCP_ENDPOINT_PATH) {
            val owner = call.verifiedMcpPatOwner(personalAccessTokenService)
            if (owner == null) {
                call.response.header(HttpHeaders.WWWAuthenticate, "Bearer")
                call.response.header(HttpHeaders.CacheControl, "no-store")
                call.respond(HttpStatusCode.Unauthorized)
                finish()
            } else {
                call.attributes.put(
                    McpRequestContextAttributeKey,
                    McpRequestContext.fromVerifiedPatOwner(owner),
                )
            }
        }
    }

    mcpStatelessStreamableHttp(
        path = MCP_ENDPOINT_PATH,
        enableDnsRebindingProtection = false,
    ) {
        val requestContext = call.attributes.getOrNull(McpRequestContextAttributeKey)
            ?: error("MCP request context missing after PAT authentication")
        createMcpServer(requestContext)
    }
}

private fun ApplicationCall.verifiedMcpPatOwner(
    personalAccessTokenService: () -> PersonalAccessTokenService,
): User? {
    val token = request.headers[HttpHeaders.Authorization].toBearerToken() ?: return null
    return try {
        personalAccessTokenService().verify(token)
    } catch (_: HttpException) {
        null
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
