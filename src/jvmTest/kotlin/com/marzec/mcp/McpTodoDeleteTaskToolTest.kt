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

class McpTodoDeleteTaskToolTest {
    @Test
    fun deleteTask_usesVerifiedPatOwnerAndPreservesDefaultAndExplicitFalse() {
        val patService = mockk<PersonalAccessTokenService>()
        val controller = mockk<ToDoApiController>()
        val requests = mutableListOf<HttpRequest<Unit>>()
        every { patService.verify("valid-test-token") } returns User(id = 42, email = "owner@example.test")
        every { controller.removeTask(capture(requests)) } returns DomainHttpResponse.Success(deletedTask())

        testApplication {
            application { configureMcpEndpoint({ patService }, { controller }) }
            val defaultResponse = callDeleteTask("""{"id":13}""")
            val falseResponse = callDeleteTask("""{"id":14,"removeWithSubtasks":false}""")

            assertThat(defaultResponse.status).isEqualTo(HttpStatusCode.OK)
            assertThat(defaultResponse.bodyAsText()).contains("Deleted task")
            assertThat(falseResponse.status).isEqualTo(HttpStatusCode.OK)
            assertThat(requests).hasSize(2)
            assertThat(requests[0].sessions[Api.Args.ARG_USER_ID]).isEqualTo("42")
            assertThat(requests[0].parameters[Api.Args.ARG_ID]).isEqualTo("13")
            assertThat(requests[0].queries["removeWithSubtasks"]).isNull()
            assertThat(requests[1].sessions[Api.Args.ARG_USER_ID]).isEqualTo("42")
            assertThat(requests[1].parameters[Api.Args.ARG_ID]).isEqualTo("14")
            assertThat(requests[1].queries["removeWithSubtasks"]).containsExactly("false")
            verify(exactly = 2) { controller.removeTask(any()) }
        }
    }

    @Test
    fun deleteTask_passesTrueForRemovingSubtasks() {
        val patService = mockk<PersonalAccessTokenService>()
        val controller = mockk<ToDoApiController>()
        val requestSlot = slot<HttpRequest<Unit>>()
        every { patService.verify("valid-test-token") } returns User(id = 42, email = "owner@example.test")
        every { controller.removeTask(capture(requestSlot)) } returns DomainHttpResponse.Success(deletedTask())

        testApplication {
            application { configureMcpEndpoint({ patService }, { controller }) }
            val response = callDeleteTask("""{"id":13,"removeWithSubtasks":true}""")

            assertThat(response.status).isEqualTo(HttpStatusCode.OK)
            assertThat(requestSlot.captured.parameters[Api.Args.ARG_ID]).isEqualTo("13")
            assertThat(requestSlot.captured.sessions[Api.Args.ARG_USER_ID]).isEqualTo("42")
            assertThat(requestSlot.captured.queries["removeWithSubtasks"]).containsExactly("true")
            verify(exactly = 1) { controller.removeTask(any()) }
        }
    }

    @Test
    fun deleteTask_rejectsInvalidIdsAndOptionTypesBeforeControllerDispatch() {
        val patService = mockk<PersonalAccessTokenService>()
        val controller = mockk<ToDoApiController>(relaxed = true)
        every { patService.verify("valid-test-token") } returns User(id = 42, email = "owner@example.test")

        testApplication {
            application { configureMcpEndpoint({ patService }, { controller }) }
            val responses = listOf(
                callDeleteTask("""{}""").bodyAsText(),
                callDeleteTask("""{"id":"13"}""").bodyAsText(),
                callDeleteTask("""{"id":0}""").bodyAsText(),
                callDeleteTask("""{"id":13.5}""").bodyAsText(),
                callDeleteTask("""{"id":13,"removeWithSubtasks":"true"}""").bodyAsText(),
                callDeleteTask("""{"id":13,"removeWithSubtasks":1}""").bodyAsText(),
                callDeleteTask("""{"id":13,"removeWithSubtasks":null}""").bodyAsText(),
                callDeleteTask("""{"id":13,"extra":true}""").bodyAsText(),
            )

            responses.forEach { assertThat(it).contains("\"isError\":true") }
            verify(exactly = 0) { controller.removeTask(any()) }
        }
    }

    @Test
    fun deleteTask_rejectsCallerIdentityOverrideBeforeControllerDispatch() {
        val patService = mockk<PersonalAccessTokenService>()
        val controller = mockk<ToDoApiController>(relaxed = true)
        every { patService.verify("valid-test-token") } returns User(id = 42, email = "owner@example.test")

        testApplication {
            application { configureMcpEndpoint({ patService }, { controller }) }
            val body = callDeleteTask("""{"id":13,"userId":999}""").bodyAsText()

            assertThat(body).contains("MCP tool arguments must not specify the caller userId")
            assertThat(body).contains("\"isError\":true")
            verify(exactly = 0) { controller.removeTask(any()) }
        }
    }

    @Test
    fun deleteTask_mapsInaccessibleTaskToToolErrorForVerifiedOwner() {
        val patService = mockk<PersonalAccessTokenService>()
        val controller = mockk<ToDoApiController>()
        val requestSlot = slot<HttpRequest<Unit>>()
        every { patService.verify("valid-test-token") } returns User(id = 42, email = "owner@example.test")
        every { controller.removeTask(capture(requestSlot)) } returns DomainHttpResponse.Error<TaskDto>(
            data = ErrorDto("Task not found"),
            httpStatusCode = 404,
        )

        testApplication {
            application { configureMcpEndpoint({ patService }, { controller }) }
            val body = callDeleteTask("""{"id":999}""").bodyAsText()

            assertThat(requestSlot.captured.sessions[Api.Args.ARG_USER_ID]).isEqualTo("42")
            assertThat(requestSlot.captured.parameters[Api.Args.ARG_ID]).isEqualTo("999")
            assertThat(body).contains("\"isError\":true")
            assertThat(body).contains("Task not found")
            verify(exactly = 1) { controller.removeTask(any()) }
        }
    }

    @Test
    fun deleteTask_isListedAsDestructiveWithOptionalBooleanAndRequiredId() {
        val patService = mockk<PersonalAccessTokenService>()
        val controller = mockk<ToDoApiController>(relaxed = true)
        every { patService.verify("valid-test-token") } returns User(id = 42, email = "owner@example.test")

        testApplication {
            application { configureMcpEndpoint({ patService }, { controller }) }
            val response = client.post(MCP_ENDPOINT_PATH) {
                header(HttpHeaders.Accept, "application/json, text/event-stream")
                contentType(ContentType.Application.Json)
                header("Mcp-Protocol-Version", "2025-11-25")
                header(HttpHeaders.Authorization, "Bearer valid-test-token")
                setBody("""{"jsonrpc":"2.0","id":2,"method":"tools/list"}""")
            }
            val body = response.bodyAsText()

            assertThat(body).contains("todo_delete_task")
            assertThat(body).contains("Destructive operation")
            assertThat(body).contains("subtasks")
            assertThat(body).contains("\"required\":[\"id\"]")
            assertThat(body).contains("\"removeWithSubtasks\"")
            assertThat(body).contains("\"type\":\"boolean\"")
        }
    }

    private suspend fun ApplicationTestBuilder.callDeleteTask(arguments: String) = client.post(MCP_ENDPOINT_PATH) {
        header(HttpHeaders.Accept, "application/json, text/event-stream")
        contentType(ContentType.Application.Json)
        header("Mcp-Protocol-Version", "2025-11-25")
        header(HttpHeaders.Authorization, "Bearer valid-test-token")
        setBody(
            """
            {
              "jsonrpc":"2.0",
              "id":4,
              "method":"tools/call",
              "params":{"name":"todo_delete_task","arguments":$arguments}
            }
            """.trimIndent(),
        )
    }

    private fun deletedTask() = TaskDto(
        id = 13,
        ownerId = 42,
        description = "Deleted task",
        addedTime = "2026-10-29T10:00:00",
        modifiedTime = "2026-10-29T10:00:00",
        parentTaskId = null,
        subTasks = emptyList(),
        isToDo = false,
        priority = 0,
        scheduler = null,
        expirationDate = null,
        shares = emptyList(),
    )
}
