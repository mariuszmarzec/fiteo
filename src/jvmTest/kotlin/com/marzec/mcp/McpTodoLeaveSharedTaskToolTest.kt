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
import com.marzec.todo.model.LeaveShareDto
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

class McpTodoLeaveSharedTaskToolTest {
    @Test
    fun leaveShare_usesVerifiedPatOwnerAndReturnsUpdatedTask() {
        val patService = mockk<PersonalAccessTokenService>()
        val controller = mockk<ToDoApiController>()
        val requestSlot = slot<HttpRequest<LeaveShareDto>>()
        every { patService.verify("valid-test-token") } returns User(id = 42, email = "sharee@example.test")
        every { controller.leaveShare(capture(requestSlot)) } returns DomainHttpResponse.Success(updatedSharedTask())

        testApplication {
            application { configureMcpEndpoint({ patService }, { controller }) }
            val response = callLeaveSharedTask("""{"id":13}""")

            assertThat(response.status).isEqualTo(HttpStatusCode.OK)
            assertThat(response.bodyAsText()).contains("Shared task")
            assertThat(requestSlot.captured.sessions[Api.Args.ARG_USER_ID]).isEqualTo("42")
            assertThat(requestSlot.captured.data.id).isEqualTo(13)
            verify(exactly = 1) { controller.leaveShare(any()) }
        }
    }

    @Test
    fun leaveShare_rejectsInvalidIdsBeforeControllerDispatch() {
        val patService = mockk<PersonalAccessTokenService>()
        val controller = mockk<ToDoApiController>(relaxed = true)
        every { patService.verify("valid-test-token") } returns User(id = 42, email = "sharee@example.test")

        testApplication {
            application { configureMcpEndpoint({ patService }, { controller }) }
            val responses = listOf(
                callLeaveSharedTask("""{}""").bodyAsText(),
                callLeaveSharedTask("""{"id":"13"}""").bodyAsText(),
                callLeaveSharedTask("""{"id":0}""").bodyAsText(),
                callLeaveSharedTask("""{"id":13.5}""").bodyAsText(),
                callLeaveSharedTask("""{"id":13,"unexpected":true}""").bodyAsText(),
            )

            responses.forEach { assertThat(it).contains("\"isError\":true") }
            verify(exactly = 0) { controller.leaveShare(any()) }
        }
    }

    @Test
    fun leaveShare_rejectsCallerIdentityOverrideBeforeControllerDispatch() {
        val patService = mockk<PersonalAccessTokenService>()
        val controller = mockk<ToDoApiController>(relaxed = true)
        every { patService.verify("valid-test-token") } returns User(id = 42, email = "sharee@example.test")

        testApplication {
            application { configureMcpEndpoint({ patService }, { controller }) }
            val body = callLeaveSharedTask("""{"id":13,"userId":999}""").bodyAsText()

            assertThat(body).contains("MCP tool arguments must not specify the caller userId")
            assertThat(body).contains("\"isError\":true")
            verify(exactly = 0) { controller.leaveShare(any()) }
        }
    }

    @Test
    fun leaveShare_mapsInaccessibleTaskToToolErrorForVerifiedOwner() {
        val patService = mockk<PersonalAccessTokenService>()
        val controller = mockk<ToDoApiController>()
        val requestSlot = slot<HttpRequest<LeaveShareDto>>()
        every { patService.verify("valid-test-token") } returns User(id = 42, email = "sharee@example.test")
        every { controller.leaveShare(capture(requestSlot)) } returns DomainHttpResponse.Error<TaskDto>(
            data = ErrorDto("Task not found"),
            httpStatusCode = 404,
        )

        testApplication {
            application { configureMcpEndpoint({ patService }, { controller }) }
            val body = callLeaveSharedTask("""{"id":999}""").bodyAsText()

            assertThat(requestSlot.captured.sessions[Api.Args.ARG_USER_ID]).isEqualTo("42")
            assertThat(requestSlot.captured.data.id).isEqualTo(999)
            assertThat(body).contains("\"isError\":true")
            assertThat(body).contains("Task not found")
            verify(exactly = 1) { controller.leaveShare(any()) }
        }
    }

    @Test
    fun leaveShare_isListedWithRequiredIdAndEffectDescription() {
        val patService = mockk<PersonalAccessTokenService>()
        val controller = mockk<ToDoApiController>(relaxed = true)
        every { patService.verify("valid-test-token") } returns User(id = 42, email = "sharee@example.test")

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

            assertThat(body).contains("todo_leave_shared_task")
            assertThat(body).contains("removes the owner's share")
            assertThat(body).contains("\"required\":[\"id\"]")
            assertThat(body).contains("\"minimum\":1")
        }
    }

    private suspend fun ApplicationTestBuilder.callLeaveSharedTask(arguments: String) = client.post(MCP_ENDPOINT_PATH) {
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
              "params":{"name":"todo_leave_shared_task","arguments":$arguments}
            }
            """.trimIndent(),
        )
    }

    private fun updatedSharedTask() = TaskDto(
        id = 13,
        ownerId = 9,
        description = "Shared task",
        addedTime = "2026-10-29T10:00:00",
        modifiedTime = "2026-10-29T10:00:00",
        parentTaskId = null,
        subTasks = emptyList(),
        isToDo = false,
        priority = 3,
        scheduler = null,
        expirationDate = null,
        shares = emptyList(),
    )
}
