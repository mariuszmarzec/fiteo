package com.marzec.mcp

import com.google.common.truth.Truth.assertThat
import com.marzec.Api
import com.marzec.fiteo.model.domain.User
import com.marzec.fiteo.model.dto.ErrorDto
import com.marzec.fiteo.model.http.HttpRequest
import com.marzec.fiteo.model.http.HttpResponse as DomainHttpResponse
import com.marzec.fiteo.services.PersonalAccessTokenService
import com.marzec.todo.ToDoApiController
import com.marzec.todo.dto.TaskDto
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Test

class McpTodoListToolTest {

    @Test
    fun todoList_usesVerifiedPatOwnerAndReturnsOnlyThatOwnersTasks() {
        val patService = mockk<PersonalAccessTokenService>()
        val controller = mockk<ToDoApiController>()
        val requestSlot = slot<HttpRequest<Unit>>()
        val ownerTask = task(id = 1, ownerId = 42, description = "owner task")
        val otherUsersTask = task(id = 2, ownerId = 99, description = "other user's task")

        every { patService.verify("valid-test-token") } returns User(
            id = 42,
            email = "owner@example.test",
        )
        every { controller.getTasks(capture(requestSlot)) } answers {
            val request = firstArg<HttpRequest<Unit>>()
            val ownerId = request.sessions[Api.Args.ARG_USER_ID]?.toInt()
            DomainHttpResponse.Success(
                if (ownerId == 42) listOf(ownerTask) else listOf(otherUsersTask),
            )
        }

        testApplication {
            application {
                configureMcpEndpoint(
                    personalAccessTokenService = { patService },
                    todoController = { controller },
                )
            }

            val response = callTodoListTool()

            assertThat(response.status).isEqualTo(HttpStatusCode.OK)
            val body = response.bodyAsText()
            assertThat(body).contains("owner task")
            assertThat(body).doesNotContain("other user's task")
            assertThat(requestSlot.captured.sessions[Api.Args.ARG_USER_ID]).isEqualTo("42")
            assertThat(body).doesNotContain("\"isError\":true")
        }
    }

    @Test
    fun todoList_rejectsCallerProvidedUserIdBeforeCallingController() {
        val patService = mockk<PersonalAccessTokenService>()
        val controller = mockk<ToDoApiController>(relaxed = true)
        every { patService.verify("valid-test-token") } returns User(
            id = 42,
            email = "owner@example.test",
        )

        testApplication {
            application {
                configureMcpEndpoint(
                    personalAccessTokenService = { patService },
                    todoController = { controller },
                )
            }

            val response = callTodoListTool(
                argumentsJson = """{"${Api.Args.ARG_USER_ID}":999}""",
            )

            assertThat(response.status).isEqualTo(HttpStatusCode.OK)
            val body = response.bodyAsText()
            assertThat(body).contains("MCP tool arguments must not specify the caller userId")
            assertThat(body).contains("\"isError\":true")
            verify(exactly = 0) { controller.getTasks(any()) }
        }
    }

    @Test
    fun todoList_mapsControllerErrorsToToolErrorsWithoutStackTraces() {
        val patService = mockk<PersonalAccessTokenService>()
        val controller = mockk<ToDoApiController>()
        every { patService.verify("valid-test-token") } returns User(
            id = 42,
            email = "owner@example.test",
        )
        every { controller.getTasks(any()) } returns DomainHttpResponse.Error(
            data = ErrorDto("Access denied"),
            httpStatusCode = 403,
        )

        testApplication {
            application {
                configureMcpEndpoint(
                    personalAccessTokenService = { patService },
                    todoController = { controller },
                )
            }

            val response = callTodoListTool()
            val body = response.bodyAsText()

            assertThat(response.status).isEqualTo(HttpStatusCode.OK)
            assertThat(body).contains("Access denied")
            assertThat(body).contains("\"isError\":true")
            assertThat(body).doesNotContain("stackTrace")
            assertThat(body).doesNotContain("valid-test-token")
        }
    }

    private suspend fun ApplicationTestBuilder.callTodoListTool(
        argumentsJson: String = "{}",
    ) = client.post(MCP_ENDPOINT_PATH) {
        header(HttpHeaders.Accept, "application/json, text/event-stream")
        contentType(ContentType.Application.Json)
        header("Mcp-Protocol-Version", "2025-11-25")
        header(HttpHeaders.Authorization, "Bearer valid-test-token")
        setBody(
            """
            {
              "jsonrpc": "2.0",
              "id": 3,
              "method": "tools/call",
              "params": {
                "name": "todo_list_tasks",
                "arguments": $argumentsJson
              }
            }
            """.trimIndent(),
        )
    }

    private fun task(id: Int, ownerId: Int, description: String) = TaskDto(
        id = id,
        ownerId = ownerId,
        description = description,
        addedTime = "2026-10-28T10:00:00",
        modifiedTime = "2026-10-28T10:00:00",
        parentTaskId = null,
        subTasks = emptyList(),
        isToDo = true,
        priority = 1,
        shares = emptyList(),
    )
}
