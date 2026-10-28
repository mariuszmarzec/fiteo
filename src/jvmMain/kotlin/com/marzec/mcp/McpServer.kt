package com.marzec.mcp

import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.mcpStatelessStreamableHttp
import io.modelcontextprotocol.kotlin.sdk.types.Implementation
import io.modelcontextprotocol.kotlin.sdk.types.ServerCapabilities
import io.ktor.server.application.Application

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
 * Network access is restricted by the deployment infrastructure. The endpoint
 * does not derive host/origin allowlists from the OpenAPI URL. PAT
 * authentication is added in the next step before this feature is released.
 */
internal fun Application.configureMcpEndpoint() {
    mcpStatelessStreamableHttp(
        path = MCP_ENDPOINT_PATH,
        enableDnsRebindingProtection = false,
    ) {
        createMcpServer()
    }
}
