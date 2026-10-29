package com.marzec.mcp

import com.google.common.truth.Truth.assertThat
import com.marzec.cheatday.CheatDayController
import com.marzec.fiteo.model.domain.User
import com.marzec.fiteo.services.PersonalAccessTokenService
import com.marzec.todo.ToDoApiController
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Test

class McpTransportSecurityTest {
    @Test
    fun acceptsPatAuthenticatedRequestWithoutOrigin() {
        val patService = validPatService()
        val todoController = mockk<ToDoApiController>(relaxed = true)
        every { todoController.getTasks(any()) } returns
            com.marzec.fiteo.model.http.HttpResponse.Success(emptyList())

        testApplication {
            application {
                configureMcpEndpoint(
                    personalAccessTokenService = { patService },
                    todoController = { todoController },
                )
            }

            val response = postMcp(body = toolCall("todo_list_tasks"))

            assertThat(response.status).isEqualTo(HttpStatusCode.OK)
            assertThat(response.bodyAsText()).contains("\"result\"")
            verify(exactly = 1) { todoController.getTasks(any()) }
        }
    }

    @Test
    fun advertisesOnlyExplicitlyAllowlistedTodoAndCheatDayTools() {
        val patService = validPatService()
        val todoController = mockk<ToDoApiController>(relaxed = true)
        val cheatDayController = mockk<CheatDayController>(relaxed = true)

        testApplication {
            application {
                configureMcpEndpoint(
                    personalAccessTokenService = { patService },
                    todoController = { todoController },
                    cheatDayController = { cheatDayController },
                )
            }

            val response = postMcp(body = """{"jsonrpc":"2.0","id":2,"method":"tools/list"}""")
            assertThat(response.status).isEqualTo(HttpStatusCode.OK)

            val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
            val tools = body["result"]!!.jsonObject["tools"]!!.jsonArray
            val names = tools.map { it.jsonObject["name"]!!.jsonPrimitive.content }.toSet()
            assertThat(names).containsExactlyElementsIn(
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
        }
    }

    @Test
    fun unknownToolNameReturnsProtocolOrToolErrorWithoutDispatch() {
        val patService = validPatService()
        val todoController = mockk<ToDoApiController>(relaxed = true)

        testApplication {
            application {
                configureMcpEndpoint(
                    personalAccessTokenService = { patService },
                    todoController = { todoController },
                )
            }

            val response = postMcp(body = toolCall("fiteo_admin_delete_user"))
            assertThat(response.status).isEqualTo(HttpStatusCode.OK)
            val payload = Json.parseToJsonElement(response.bodyAsText()).jsonObject
            val hasProtocolError = "error" in payload
            val hasToolError = payload["result"]?.jsonObject?.get("isError")?.jsonPrimitive?.booleanOrNull == true
            assertThat(hasProtocolError || hasToolError).isTrue()
            verify(exactly = 0) { todoController.getTasks(any()) }
        }
    }

    @Test
    fun rejectsMalformedJsonRpcRequest() {
        val patService = validPatService()
        val todoController = mockk<ToDoApiController>(relaxed = true)

        testApplication {
            application {
                configureMcpEndpoint(
                    personalAccessTokenService = { patService },
                    todoController = { todoController },
                )
            }

            val response = postMcp(body = """{"jsonrpc":""")
            assertThat(response.status.value >= 400).isTrue()
            assertThat(response.bodyAsText()).doesNotContain("valid-test-token")
            verify(exactly = 0) { todoController.getTasks(any()) }
        }
    }

    @Test
    fun endpointIsNotMountedUnderTestRoutePrefix() {
        val patService = mockk<PersonalAccessTokenService>()
        val todoController = mockk<ToDoApiController>(relaxed = true)

        testApplication {
            application {
                configureMcpEndpoint(
                    personalAccessTokenService = { patService },
                    todoController = { todoController },
                )
            }

            val response = postMcp(
                path = "/test$mcpSuffix",
                body = """{"jsonrpc":"2.0","id":2,"method":"tools/list"}""",
            )

            assertThat(response.status).isEqualTo(HttpStatusCode.NotFound)
            verify(exactly = 0) { patService.verify(any()) }
            verify(exactly = 0) { todoController.getTasks(any()) }
        }
    }

    private fun validPatService(): PersonalAccessTokenService = mockk {
        every { verify("valid-test-token") } returns User(
            id = 42,
            email = "owner@example.test",
        )
    }

    private suspend fun ApplicationTestBuilder.postMcp(
        body: String,
        path: String = MCP_ENDPOINT_PATH,
    ) = client.post(path) {
        header(HttpHeaders.Accept, "application/json, text/event-stream")
        contentType(ContentType.Application.Json)
        header("Mcp-Protocol-Version", "2025-11-25")
        header(HttpHeaders.Authorization, "Bearer valid-test-token")
        setBody(body)
    }

    private fun toolCall(name: String) = """
        {
          "jsonrpc": "2.0",
          "id": 4,
          "method": "tools/call",
          "params": {"name": "$name", "arguments": {}}
        }
    """.trimIndent()

    private companion object {
        const val mcpSuffix = "/mcp"
    }
}
