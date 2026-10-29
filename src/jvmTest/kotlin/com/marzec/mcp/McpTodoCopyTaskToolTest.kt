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

class McpTodoCopyTaskToolTest {
    @Test
    fun copyTask_usesVerifiedPatOwnerAndReturnsCopiedTask() {
        val patService = mockk<PersonalAccessTokenService>()
        val controller = mockk<ToDoApiController>()
        val requestSlot = slot<HttpRequest<Unit>>()
        every { patService.verify("valid-test-token") } returns User(id = 42, email = "owner@example.test")
        every { controller.copyTasks(capture(requestSlot)) } returns DomainHttpResponse.Success(copiedTask())

        testApplication {
            application { configureMcpEndpoint({ patService }, { controller }) }
            val response = callCopyTask("""{"id":7}""")

            assertThat(response.status).isEqualTo(HttpStatusCode.OK)
            assertThat(response.bodyAsText()).contains("Copied task")
            assertThat(requestSlot.captured.sessions[Api.Args.ARG_USER_ID]).isEqualTo("42")
            assertThat(requestSlot.captured.parameters[Api.Args.ARG_ID]).isEqualTo("7")
            verify(exactly = 1) { controller.copyTasks(any()) }
        }
    }

    @Test
    fun copyTask_rejectsInvalidIdsBeforeControllerDispatch() {
        val patService = mockk<PersonalAccessTokenService>()
        val controller = mockk<ToDoApiController>(relaxed = true)
        every { patService.verify("valid-test-token") } returns User(id = 42, email = "owner@example.test")

        testApplication {
            application { configureMcpEndpoint({ patService }, { controller }) }
            val responses = listOf(
                callCopyTask("""{}""").bodyAsText(),
                callCopyTask("""{"id":"7"}""").bodyAsText(),
                callCopyTask("""{"id":0}""").bodyAsText(),
                callCopyTask("""{"id":-7}""").bodyAsText(),
                callCopyTask("""{"id":7.5}""").bodyAsText(),
                callCopyTask("""{"id":7,"unexpected":true}""").bodyAsText(),
            )

            responses.forEach { assertThat(it).contains("\"isError\":true") }
            verify(exactly = 0) { controller.copyTasks(any()) }
        }
    }

    @Test
    fun copyTask_rejectsCallerIdentityOverrideBeforeControllerDispatch() {
        val patService = mockk<PersonalAccessTokenService>()
        val controller = mockk<ToDoApiController>(relaxed = true)
        every { patService.verify("valid-test-token") } returns User(id = 42, email = "owner@example.test")

        testApplication {
            application { configureMcpEndpoint({ patService }, { controller }) }
            val body = callCopyTask("""{"id":7,"userId":999}""").bodyAsText()

            assertThat(body).contains("MCP tool arguments must not specify the caller userId")
            assertThat(body).contains("\"isError\":true")
            verify(exactly = 0) { controller.copyTasks(any()) }
        }
    }

    @Test
    fun copyTask_doesNotExposeTasksThePatOwnerCannotAccess() {
        val patService = mockk<PersonalAccessTokenService>()
        val controller = mockk<ToDoApiController>()
        val requestSlot = slot<HttpRequest<Unit>>()
        every { patService.verify("valid-test-token") } returns User(id = 42, email = "owner@example.test")
        every { controller.copyTasks(capture(requestSlot)) } returns DomainHttpResponse.Error<TaskDto>(
            data = ErrorDto("Task not found"),
            httpStatusCode = 404,
        )

        testApplication {
            application { configureMcpEndpoint({ patService }, { controller }) }
            val body = callCopyTask("""{"id":999}""").bodyAsText()

            assertThat(requestSlot.captured.sessions[Api.Args.ARG_USER_ID]).isEqualTo("42")
            assertThat(requestSlot.captured.parameters[Api.Args.ARG_ID]).isEqualTo("999")
            assertThat(body).contains("\"isError\":true")
            assertThat(body).contains("Task not found")
            assertThat(body).doesNotContain("private task description")
            verify(exactly = 1) { controller.copyTasks(any()) }
        }
    }

    @Test
    fun copyTask_isListedAsAStateChangingToolWithRequiredId() {
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

            assertThat(body).contains("todo_copy_task")
            assertThat(body).contains("not a read-only lookup")
            assertThat(body).contains("\\\"required\\\":[\\\"id\\\"]")
            assertThat(body).contains("\\\"minimum\\\":1")
        }
    }

    private suspend fun ApplicationTestBuilder.callCopyTask(arguments: String) = client.post(MCP_ENDPOINT_PATH) {
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
              "params":{"name":"todo_copy_task","arguments":$arguments}
            }
            """.trimIndent(),
        )
    }

    private fun copiedTask() = TaskDto(
        id = 124,
        ownerId = 42,
        description = "Copied task",
        addedTime = "2026-10-29T10:00:00",
        modifiedTime = "2026-10-29T10:00:00",
        parentTaskId = null,
        subTasks = emptyList(),
        isToDo = true,
        priority = 1,
        scheduler = null,
        expirationDate = null,
        shares = emptyList(),
    )
}
