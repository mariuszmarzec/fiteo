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
import com.marzec.todo.model.UpdateTaskDto
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

class McpTodoUpdateTaskToolTest {
    @Test
    fun updateTask_usesVerifiedPatOwnerAndPreservesExplicitNullableClear() {
        val patService = mockk<PersonalAccessTokenService>()
        val controller = mockk<ToDoApiController>()
        val requestSlot = slot<HttpRequest<UpdateTaskDto>>()
        every { patService.verify("valid-test-token") } returns User(id = 42, email = "owner@example.test")
        every { controller.updateTask(capture(requestSlot)) } answers {
            DomainHttpResponse.Success(task(description = "Updated task", parentTaskId = null))
        }

        testApplication {
            application { configureMcpEndpoint({ patService }, { controller }) }
            val response = callUpdateTask(
                """{"id":7,"task":{"description":"Updated task","parentTaskId":{"value":null}}}""",
            )
            assertThat(response.status).isEqualTo(HttpStatusCode.OK)
            assertThat(response.bodyAsText()).contains("Updated task")
            val request = requestSlot.captured
            assertThat(request.sessions[Api.Args.ARG_USER_ID]).isEqualTo("42")
            assertThat(request.parameters[Api.Args.ARG_ID]).isEqualTo("7")
            assertThat(request.data.description).isEqualTo("Updated task")
            assertThat(request.data.parentTaskId).isNotNull()
            assertThat(request.data.parentTaskId?.value).isNull()
            verify(exactly = 1) { controller.updateTask(any()) }
        }
    }

    @Test
    fun updateTask_rejectsCallerIdentityOverrideBeforeCallingController() {
        val patService = mockk<PersonalAccessTokenService>()
        val controller = mockk<ToDoApiController>(relaxed = true)
        every { patService.verify("valid-test-token") } returns User(id = 42, email = "owner@example.test")

        testApplication {
            application { configureMcpEndpoint({ patService }, { controller }) }
            val body = callUpdateTask("""{"id":7,"task":{"description":"Nope"},"userId":999}""").bodyAsText()
            assertThat(body).contains("MCP tool arguments must not specify the caller userId")
            assertThat(body).contains("\"isError\":true")
            verify(exactly = 0) { controller.updateTask(any()) }
        }
    }

    @Test
    fun updateTask_rejectsInvalidIdsEmptyPatchesAndMalformedDatesBeforeDispatch() {
        val patService = mockk<PersonalAccessTokenService>()
        val controller = mockk<ToDoApiController>(relaxed = true)
        every { patService.verify("valid-test-token") } returns User(id = 42, email = "owner@example.test")

        testApplication {
            application { configureMcpEndpoint({ patService }, { controller }) }
            val invalidId = callUpdateTask("""{"id":"bad","task":{"description":"x"}}""").bodyAsText()
            val emptyPatch = callUpdateTask("""{"id":7,"task":{}}""").bodyAsText()
            val invalidDate = callUpdateTask("""{"id":7,"task":{"expirationDate":{"value":"not-a-date"}}}""").bodyAsText()
            val invalidWrapper = callUpdateTask("""{"id":7,"task":{"parentTaskId":{"value":null,"ownerId":99}}}""").bodyAsText()
            val flatPayload = callUpdateTask("""{"id":7,"description":"flat payload"}""").bodyAsText()

            assertThat(invalidId).contains("\"isError\":true")
            assertThat(emptyPatch).contains("\"isError\":true")
            assertThat(invalidDate).contains("\"isError\":true")
            assertThat(invalidWrapper).contains("\"isError\":true")
            assertThat(flatPayload).contains("\"isError\":true")
            verify(exactly = 0) { controller.updateTask(any()) }
        }
    }

    @Test
    fun updateTask_mapsControllerConstraintsToMcpErrors() {
        val patService = mockk<PersonalAccessTokenService>()
        val controller = mockk<ToDoApiController>()
        every { patService.verify("valid-test-token") } returns User(id = 42, email = "owner@example.test")
        every { controller.updateTask(any()) } returns DomainHttpResponse.Error<TaskDto>(
            data = ErrorDto("Scheduled task can't have parent"),
            httpStatusCode = 400,
        )

        testApplication {
            application { configureMcpEndpoint({ patService }, { controller }) }
            val body = callUpdateTask(
                """{"id":7,"task":{"parentTaskId":{"value":12},"scheduler":{"value":{"hour":10,"minute":15,"startDate":"2026-10-28T10:00:00","daysOfWeek":[],"dayOfMonth":0,"repeatCount":-1,"repeatInEveryPeriod":1,"type":"OneShot"}}}}""",
            ).bodyAsText()
            assertThat(body).contains("Scheduled task can't have parent")
            assertThat(body).contains("\"isError\":true")
        }
    }

    @Test
    fun updateTask_redactsInternalControllerErrors() {
        val patService = mockk<PersonalAccessTokenService>()
        val controller = mockk<ToDoApiController>()
        every { patService.verify("valid-test-token") } returns User(id = 42, email = "owner@example.test")
        every { controller.updateTask(any()) } returns DomainHttpResponse.Error<TaskDto>(
            data = ErrorDto("database host and stack details"),
            httpStatusCode = 500,
        )

        testApplication {
            application { configureMcpEndpoint({ patService }, { controller }) }
            val body = callUpdateTask("""{"id":7,"task":{"description":"Update"}}""").bodyAsText()
            assertThat(body).contains("internal error")
            assertThat(body).doesNotContain("database host and stack details")
            assertThat(body).doesNotContain("valid-test-token")
        }
    }

    @Test
    fun updateTask_isListedInMcpToolCatalogWithExplicitFields() {
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
            assertThat(body).contains("todo_update_task")
            assertThat(body).contains("parentTaskId")
            assertThat(body).contains("expirationDate")
            assertThat(body).contains("\"required\":[\"id\",\"task\"]")
            assertThat(body).contains("additionalProperties")
        }
    }

    private suspend fun ApplicationTestBuilder.callUpdateTask(arguments: String) = client.post(MCP_ENDPOINT_PATH) {
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
              "params":{"name":"todo_update_task","arguments":$arguments}
            }
            """.trimIndent(),
        )
    }

    private fun task(description: String, parentTaskId: Int?) = TaskDto(
        id = 7,
        ownerId = 42,
        description = description,
        addedTime = "2026-10-28T10:00:00",
        modifiedTime = "2026-10-28T10:00:00",
        parentTaskId = parentTaskId,
        subTasks = emptyList(),
        isToDo = true,
        priority = 2,
        scheduler = null,
        expirationDate = null,
        shares = emptyList(),
    )
}
