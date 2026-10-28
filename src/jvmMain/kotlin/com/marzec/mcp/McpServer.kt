package com.marzec.mcp

import io.ktor.server.application.Application
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
 * Registers the stateless Streamable HTTP MCP endpoint using the official SDK route helper.
 *
 * MCP JSON-RPC responses are handled by the dedicated content converter registered alongside
 * the existing REST JSON converter. Network host/origin restrictions belong to deployment
 * infrastructure; PAT authentication is added in the next step before release.
 */
internal fun Application.configureMcpEndpoint() {
    mcpStatelessStreamableHttp(
        path = MCP_ENDPOINT_PATH,
        enableDnsRebindingProtection = false,
    ) {
        createMcpServer()
    }
}
