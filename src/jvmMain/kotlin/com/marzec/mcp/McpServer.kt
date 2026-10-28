package com.marzec.mcp

import com.marzec.exceptions.HttpException
import com.marzec.exceptions.HttpStatus
import com.marzec.fiteo.model.domain.User
import com.marzec.fiteo.model.http.HttpResponse
import com.marzec.fiteo.services.PersonalAccessTokenService
import com.marzec.todo.ToDoApiController
import com.marzec.todo.dto.TaskDto
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
import io.modelcontextprotocol.kotlin.sdk.types.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.types.Implementation
import io.modelcontextprotocol.kotlin.sdk.types.McpJson
import io.modelcontextprotocol.kotlin.sdk.types.ServerCapabilities
import io.modelcontextprotocol.kotlin.sdk.types.TextContent
import io.modelcontextprotocol.kotlin.sdk.types.ToolSchema
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.buildJsonObject

internal const val MCP_ENDPOINT_PATH = "/mcp"

internal fun createMcpServer(
    requestContext: McpRequestContext,
    todoController: ToDoApiController,
): Server = Server(
    serverInfo = Implementation(
        name = "fiteo",
        version = "1.0.0",
    ),
    options = ServerOptions(
        capabilities = ServerCapabilities(
            tools = ServerCapabilities.Tools(listChanged = false),
        ),
    ),
).apply {
    addTool(
        name = "todo_list_tasks",
        description = "List ToDo tasks belonging to the owner of the authenticated PAT. Returns a JSON array of TaskDto objects. This tool accepts no arguments.",
        inputSchema = ToolSchema(
            properties = buildJsonObject {},
            required = emptyList(),
        ),
    ) { request ->
        val arguments = request.params.arguments ?: buildJsonObject {}
        try {
            val httpRequest = requestContext.createRequest(
                data = Unit,
                arguments = arguments,
            )
            if (arguments.isNotEmpty()) {
                throw HttpException(
                    "todo_list_tasks does not accept arguments",
                    HttpStatus.BAD_REQUEST,
                )
            }

            when (val response = todoController.getTasks(httpRequest)) {
                is HttpResponse.Success -> CallToolResult(
                    content = listOf(
                        TextContent(
                            text = McpJson.encodeToString(
                                ListSerializer(TaskDto.serializer()),
                                response.data,
                            ),
                        ),
                    ),
                )

                is HttpResponse.Error -> toolErrorResult(
                    if (response.httpStatusCode >= 500) {
                        "Unable to list tasks because of an internal error."
                    } else {
                        response.data.reason
                    },
                )
            }
        } catch (exception: HttpException) {
            toolErrorResult(exception.message ?: "Invalid todo_list_tasks arguments")
        } catch (_: Exception) {
            toolErrorResult("Unable to list tasks because of an internal error.")
        }
    }
}

/**
 * Registers PAT authentication before the SDK's Streamable HTTP handler and retains the
 * verified owner's request context only on the current Ktor call.
 */
internal fun Application.configureMcpEndpoint(
    personalAccessTokenService: () -> PersonalAccessTokenService,
    todoController: () -> ToDoApiController,
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
        createMcpServer(requestContext, todoController())
    }
}

private fun toolErrorResult(message: String) = CallToolResult(
    content = listOf(TextContent(text = message)),
    isError = true,
)

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
