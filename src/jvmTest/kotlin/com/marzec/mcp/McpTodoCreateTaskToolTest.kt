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
import com.marzec.todo.model.CreateTaskDto
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

class McpTodoCreateTaskToolTest {
    @Test
    fun createTask_usesPatOwnerAndReturnsCreatedTask() {
        val patService = mockk<PersonalAccessTokenService>()
        val controller = mockk<ToDoApiController>()
        val requestSlot = slot<HttpRequest<CreateTaskDto>>()
        every { patService.verify("valid-test-token") } returns User(id = 42, email = "owner@example.test")
        every { controller.addTask(capture(requestSlot)) } answers {
            val request = firstArg<HttpRequest<CreateTaskDto>>()
            assertThat(request.sessions[Api.Args.ARG_USER_ID]).isEqualTo("42")
            assertThat(request.data.description).isEqualTo("Created via MCP")
            DomainHttpResponse.Success(task(request.data.description, request.data.isToDo ?: true))
        }

        testApplication {
            application { configureMcpEndpoint({ patService }, { controller }) }
            val response = callCreateTask("""{"task":{"description":"Created via MCP","isToDo":false}}""")
            assertThat(response.status).isEqualTo(HttpStatusCode.OK)
            val body = response.bodyAsText()
            assertThat(body).contains("Created via MCP")
            assertThat(body).contains("\\\"isToDo\\\":false")
            verify(exactly = 1) { controller.addTask(any()) }
        }
    }

    @Test
    fun createTask_rejectsCallerIdentityOverrideBeforeCallingController() {
        val patService = mockk<PersonalAccessTokenService>()
        val controller = mockk<ToDoApiController>(relaxed = true)
        every { patService.verify("valid-test-token") } returns User(id = 42, email = "owner@example.test")

        testApplication {
            application { configureMcpEndpoint({ patService }, { controller }) }
            val response = callCreateTask("""{"description":"nope","userId":999}""")
            val body = response.bodyAsText()
            assertThat(body).contains("MCP tool arguments must not specify the caller userId")
            assertThat(body).contains("\"isError\":true")
            verify(exactly = 0) { controller.addTask(any()) }
        }
    }

    @Test
    fun createTask_rejectsBlankDescriptionAndMalformedInputsWithoutCallingController() {
        val patService = mockk<PersonalAccessTokenService>()
        val controller = mockk<ToDoApiController>(relaxed = true)
        every { patService.verify("valid-test-token") } returns User(id = 42, email = "owner@example.test")

        testApplication {
            application { configureMcpEndpoint({ patService }, { controller }) }
            val blank = callCreateTask("""{"task":{"description":"  "}}""").bodyAsText()
            val flatPayload = callCreateTask("""{"description":"flat payload"}""").bodyAsText()
            assertThat(blank).contains("\"isError\":true")
            assertThat(flatPayload).contains("\"isError\":true")
            val malformed = callCreateTask("""{"task":{"description":"valid","priority":"high"}}""").bodyAsText()
            assertThat(malformed).contains("\"isError\":true")
            val malformedScheduler = callCreateTask(
                """{"task":{"description":"valid","scheduler":{"hour":10,"minute":15,"startDate":"not-a-date","daysOfWeek":[],"dayOfMonth":0,"repeatCount":-1,"repeatInEveryPeriod":1,"type":"OneShot"}}}""",
            ).bodyAsText()
            assertThat(malformedScheduler).contains("\"isError\":true")
            verify(exactly = 0) { controller.addTask(any()) }
        }
    }

    @Test
    fun createTask_redactsInternalControllerErrors() {
        val patService = mockk<PersonalAccessTokenService>()
        val controller = mockk<ToDoApiController>()
        every { patService.verify("valid-test-token") } returns User(id = 42, email = "owner@example.test")
        every { controller.addTask(any()) } returns DomainHttpResponse.Error<TaskDto>(
            data = ErrorDto("database host and stack details"),
            httpStatusCode = 500,
        )

        testApplication {
            application { configureMcpEndpoint({ patService }, { controller }) }
            val body = callCreateTask("""{"task":{"description":"Will fail"}}""").bodyAsText()
            assertThat(body).contains("internal error")
            assertThat(body).doesNotContain("database host and stack details")
            assertThat(body).doesNotContain("valid-test-token")
        }
    }


    @Test
    fun createTask_isListedWithNestedTaskSchema() {
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
            assertThat(body).contains("todo_create_task")
            assertThat(body).contains("\"task\"")
            assertThat(body).contains("\"required\":[\"task\"]")
            assertThat(body).contains("\"properties\":{\"task\":")
            assertThat(body).contains("CreateTaskDto")
        }
    }

    private suspend fun ApplicationTestBuilder.callCreateTask(arguments: String) = client.post(MCP_ENDPOINT_PATH) {
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
              "params":{"name":"todo_create_task","arguments":$arguments}
            }
            """.trimIndent(),
        )
    }

    private fun task(description: String, isToDo: Boolean) = TaskDto(
        id = 51,
        ownerId = 42,
        description = description,
        addedTime = "2026-10-28T10:00:00",
        modifiedTime = "2026-10-28T10:00:00",
        parentTaskId = null,
        subTasks = emptyList(),
        isToDo = isToDo,
        priority = 1,
        scheduler = null,
        expirationDate = null,
        shares = emptyList(),
    )
}
