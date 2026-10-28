package com.marzec.mcp

import com.marzec.fiteo.BuildKonfig
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.mcpStatelessStreamableHttp
import io.modelcontextprotocol.kotlin.sdk.types.Implementation
import io.modelcontextprotocol.kotlin.sdk.types.ServerCapabilities
import io.ktor.server.application.Application
import java.net.URI

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
 * Registers the stateless Streamable HTTP MCP endpoint.
 *
 * The allowed host/origin values are derived from the configured public API URL.
 * Localhost-only defaults keep the endpoint closed to arbitrary hosts when no
 * deployment URL is configured. PAT authentication is added in the next step
 * before this feature is released.
 */
internal fun Application.configureMcpEndpoint() {
    val configuredHost = runCatching {
        URI(BuildKonfig.OPENAPI_SERVER_URL).host
    }.getOrNull()?.takeIf { it.isNotBlank() }

    val allowedHosts = configuredHost?.let(::listOf)
        ?: listOf("localhost", "127.0.0.1", "[::1]")

    mcpStatelessStreamableHttp(
        path = MCP_ENDPOINT_PATH,
        enableDnsRebindingProtection = true,
        allowedHosts = allowedHosts,
        allowedOrigins = allowedHosts,
    ) {
        createMcpServer()
    }
}
