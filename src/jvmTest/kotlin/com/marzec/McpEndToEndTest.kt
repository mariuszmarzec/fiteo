package com.marzec

import com.google.common.truth.Truth.assertThat
import com.marzec.Api.Headers
import com.marzec.fiteo.PatApiPath
import com.marzec.fiteo.model.dto.CreatePersonalAccessTokenRequestDto
import com.marzec.fiteo.model.dto.PersonalAccessTokenCreationDto
import com.marzec.mcp.MCP_ENDPOINT_PATH
import com.marzec.todo.ApiPath as TodoApiPath
import io.ktor.client.config
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.sse.SSE
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.modelcontextprotocol.kotlin.sdk.client.Client as McpClient
import io.modelcontextprotocol.kotlin.sdk.client.StreamableHttpClientTransport
import io.modelcontextprotocol.kotlin.sdk.types.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.types.Implementation
import io.modelcontextprotocol.kotlin.sdk.types.TextContent
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Test

class McpEndToEndTest {
    @Test
    fun officialClientExercisesTodoAndCheatDayOverStreamableHttp() {
        withDefaultMockTestApplication {
            authToken = registerAndLogin()
            val sessionAuthorization = authToken!!
            val pat = postWithAuth<CreatePersonalAccessTokenRequestDto, PersonalAccessTokenCreationDto>(
                PatApiPath.TOKENS,
                CreatePersonalAccessTokenRequestDto(name = "MCP E2E test token", expiresIn = "NEVER"),
            )

            val authenticatedHttpClient = client.config {
                install(SSE)
                defaultRequest {
                    header(HttpHeaders.Authorization, "Bearer ${pat.token}")
                }
            }
            val mcpClient = McpClient(
                clientInfo = Implementation(name = "fiteo-e2e-test", version = "1.0.0"),
            )

            try {
                mcpClient.connect(
                    StreamableHttpClientTransport(
                        client = authenticatedHttpClient,
                        url = "http://localhost$MCP_ENDPOINT_PATH",
                    ),
                )

                assertThat(mcpClient.serverVersion?.name).isEqualTo("fiteo")
                val tools = mcpClient.listTools().tools.map { it.name }.toSet()
                assertThat(tools).containsExactlyElementsIn(
                    setOf(
                        "todo_list_tasks",
                        "todo_create_task",
                        "todo_update_task",
                        "todo_mark_tasks",
                        "todo_copy_task",
                        "todo_delete_task",
                        "todo_leave_shared_task",
                        "cheatday_list_weights",
                        "cheatday_get_weight",
                        "cheatday_create_weight",
                        "cheatday_update_weight",
                        "cheatday_delete_weight",
                    ),
                )

                val taskDescription = "MCP E2E lifecycle task"
                val createdTask = mcpClient.callTool(
                    "todo_create_task",
                    mapOf("task" to mapOf("description" to taskDescription, "isToDo" to true)),
                ).requireSuccessText()
                val taskId = Json.parseToJsonElement(createdTask).jsonObject
                    .getValue("id").jsonPrimitive.int

                val listedTasks = mcpClient.callTool("todo_list_tasks", emptyMap()).requireSuccessText()
                assertThat(listedTasks).contains(taskDescription)

                val updatedTaskDescription = "MCP E2E lifecycle task updated"
                mcpClient.callTool(
                    "todo_update_task",
                    mapOf("id" to taskId, "task" to mapOf("description" to updatedTaskDescription)),
                ).requireSuccessText()

                // The authenticated REST endpoint remains usable after MCP mutations.
                val restTasks = client.get(TodoApiPath.TASKS) {
                    header(Headers.AUTHORIZATION, sessionAuthorization)
                }
                assertThat(restTasks.status).isEqualTo(HttpStatusCode.OK)
                assertThat(restTasks.bodyAsText()).contains(updatedTaskDescription)

                mcpClient.callTool("todo_delete_task", mapOf("id" to taskId)).requireSuccessText()
                val tasksAfterDelete = Json.parseToJsonElement(
                    mcpClient.callTool("todo_list_tasks", emptyMap()).requireSuccessText(),
                ).jsonArray
                assertThat(tasksAfterDelete).isEmpty()

                val weightDate = "2026-10-29T08:30:00"
                val createdWeight = mcpClient.callTool(
                    "cheatday_create_weight",
                    mapOf("value" to 72.5, "date" to weightDate),
                ).requireSuccessText()
                val weightId = Json.parseToJsonElement(createdWeight).jsonObject
                    .getValue("id").jsonPrimitive.int

                val listedWeights = mcpClient.callTool("cheatday_list_weights", emptyMap()).requireSuccessText()
                assertThat(listedWeights).contains(weightDate)

                val fetchedWeight = mcpClient.callTool(
                    "cheatday_get_weight",
                    mapOf("id" to weightId),
                ).requireSuccessText()
                assertThat(fetchedWeight).contains("72.5")

                val updatedWeight = mcpClient.callTool(
                    "cheatday_update_weight",
                    mapOf("id" to weightId, "value" to 73.25),
                ).requireSuccessText()
                assertThat(updatedWeight).contains("73.25")

                mcpClient.callTool("cheatday_delete_weight", mapOf("id" to weightId)).requireSuccessText()
                val weightsAfterDelete = Json.parseToJsonElement(
                    mcpClient.callTool("cheatday_list_weights", emptyMap()).requireSuccessText(),
                ).jsonArray
                assertThat(weightsAfterDelete).isEmpty()

                val missingTaskResult = mcpClient.callTool(
                    "todo_update_task",
                    mapOf("id" to 999999, "task" to mapOf("description" to "missing task")),
                )
                assertThat(missingTaskResult.isError).isEqualTo(true)
                assertThat(missingTaskResult.textPayload().lowercase()).contains("task")
            } finally {
                mcpClient.close()
                authenticatedHttpClient.close()
            }
        }
    }

    private fun CallToolResult.requireSuccessText(): String {
        val text = textPayload()
        check(isError != true) { "MCP tool call failed: $text" }
        return text
    }

    private fun CallToolResult.textPayload(): String =
        content.filterIsInstance<TextContent>().joinToString("\n") { it.text }
}
