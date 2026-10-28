package com.marzec.mcp

import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.StreamableHttpServerTransport
import io.modelcontextprotocol.kotlin.sdk.types.Implementation
import io.modelcontextprotocol.kotlin.sdk.types.McpJson
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
 * Registers the stateless Streamable HTTP MCP endpoint.
 *
 * MCP uses its own route-scoped JSON configuration so required protocol fields and
 * optional null fields are serialized according to the MCP SDK's wire format without
 * changing serialization of the existing REST API.
 *
 * Network access is restricted by deployment infrastructure. PAT authentication is
 * added in the next step before this feature is released.
 */
internal fun Application.configureMcpEndpoint() {
    routing {
        route(MCP_ENDPOINT_PATH) {
            install(ContentNegotiation) {
                json(McpJson)
            }

            post {
                val transport = StreamableHttpServerTransport(
                    StreamableHttpServerTransport.Configuration(enableJsonResponse = true),
                ).also { it.setSessionIdGenerator(null) }
                val session = createMcpServer().createSession(transport)

                try {
                    transport.handleRequest(null, call)
                } finally {
                    session.close()
                }
            }

            get {
                rejectUnsupportedMethod()
            }

            delete {
                rejectUnsupportedMethod()
            }
        }
    }
}

private suspend fun io.ktor.server.application.ApplicationCall.rejectUnsupportedMethod() {
    response.header(HttpHeaders.Allow, HttpMethod.Post.value)
    respond(HttpStatusCode.MethodNotAllowed)
}
