package com.marzec.mcp

import com.google.common.truth.Truth.assertThat
import com.marzec.Api
import com.marzec.exceptions.HttpException
import com.marzec.exceptions.HttpStatus
import com.marzec.fiteo.model.domain.User
import com.marzec.fiteo.model.http.HttpRequest
import com.marzec.fiteo.model.http.HttpResponse as DomainHttpResponse
import com.marzec.fiteo.services.PersonalAccessTokenService
import com.marzec.todo.ToDoApiController
import com.marzec.todo.model.MarkAsToDoDto
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
import org.junit.Test

class McpTodoMarkAsToDoToolTest {
    @Test
    fun markTasks_setsAndClearsStatusUsingVerifiedPatOwner() {
        val patService = mockk<PersonalAccessTokenService>()
        val controller = mockk<ToDoApiController>()
        val requests = mutableListOf<HttpRequest<MarkAsToDoDto>>()
        every { patService.verify("valid-test-token") } returns User(id = 42, email = "owner@example.test")
        every { controller.markAsToDo(capture(requests)) } answers {
            DomainHttpResponse.Success(emptyList())
        }

        testApplication {
            application { configureMcpEndpoint({ patService }, { controller }) }
            val marked = callMarkTasks("""{"isToDo":true,"taskIds":[7,8]}""")
            val unmarked = callMarkTasks("""{"isToDo":false,"taskIds":[9]}""")

            assertThat(marked.status).isEqualTo(HttpStatusCode.OK)
            assertThat(unmarked.status).isEqualTo(HttpStatusCode.OK)
            assertThat(marked.bodyAsText()).contains("[]")
            assertThat(unmarked.bodyAsText()).contains("[]")
            assertThat(requests).hasSize(2)
            assertThat(requests[0].sessions[Api.Args.ARG_USER_ID]).isEqualTo("42")
            assertThat(requests[0].data.isToDo).isTrue()
            assertThat(requests[0].data.taskIds).containsExactly(7, 8).inOrder()
            assertThat(requests[1].sessions[Api.Args.ARG_USER_ID]).isEqualTo("42")
            assertThat(requests[1].data.isToDo).isFalse()
            assertThat(requests[1].data.taskIds).containsExactly(9)
            verify(exactly = 2) { controller.markAsToDo(any()) }
        }
    }

    @Test
    fun markTasks_rejectsCallerIdentityOverrideBeforeControllerDispatch() {
        val patService = mockk<PersonalAccessTokenService>()
        val controller = mockk<ToDoApiController>(relaxed = true)
        every { patService.verify("valid-test-token") } returns User(id = 42, email = "owner@example.test")

        testApplication {
            application { configureMcpEndpoint({ patService }, { controller }) }
            val body = callMarkTasks("""{"isToDo":true,"taskIds":[7],"userId":999}""").bodyAsText()

            assertThat(body).contains("MCP tool arguments must not specify the caller userId")
            assertThat(body).contains("\"isError\":true")
            verify(exactly = 0) { controller.markAsToDo(any()) }
        }
    }

    @Test
    fun markTasks_rejectsMissingInvalidOrEmptyArgumentsBeforeControllerDispatch() {
        val patService = mockk<PersonalAccessTokenService>()
        val controller = mockk<ToDoApiController>(relaxed = true)
        every { patService.verify("valid-test-token") } returns User(id = 42, email = "owner@example.test")

        testApplication {
            application { configureMcpEndpoint({ patService }, { controller }) }
            val responses = listOf(
                callMarkTasks("""{"isToDo":"true","taskIds":[7]}""").bodyAsText(),
                callMarkTasks("""{"isToDo":true,"taskIds":[]}""").bodyAsText(),
                callMarkTasks("""{"isToDo":true,"taskIds":[0]}""").bodyAsText(),
                callMarkTasks("""{"isToDo":true,"taskIds":["7"]}""").bodyAsText(),
                callMarkTasks("""{"isToDo":true}""").bodyAsText(),
                callMarkTasks("""{"isToDo":true,"taskIds":[7],"extra":1}""").bodyAsText(),
            )

            responses.forEach { assertThat(it).contains("\"isError\":true") }
            verify(exactly = 0) { controller.markAsToDo(any()) }
        }
    }

    @Test
    fun markTasks_mapsControllerErrorsToMcpErrorsAndRedactsInternalFailures() {
        val patService = mockk<PersonalAccessTokenService>()
        val controller = mockk<ToDoApiController>()
        every { patService.verify("valid-test-token") } returns User(id = 42, email = "owner@example.test")
        every { controller.markAsToDo(any()) } returns DomainHttpResponse.Error<List<com.marzec.todo.dto.TaskDto>>(
            data = com.marzec.fiteo.model.dto.ErrorDto("database internals"),
            httpStatusCode = 500,
        )

        testApplication {
            application { configureMcpEndpoint({ patService }, { controller }) }
            val body = callMarkTasks("""{"isToDo":true,"taskIds":[7]}""").bodyAsText()

            assertThat(body).contains("internal error")
            assertThat(body).doesNotContain("database internals")
        }
    }

    @Test
    fun markTasks_redactsInternalHttpExceptions() {
        val patService = mockk<PersonalAccessTokenService>()
        val controller = mockk<ToDoApiController>()
        every { patService.verify("valid-test-token") } returns User(id = 42, email = "owner@example.test")
        every { controller.markAsToDo(any()) } throws HttpException("database host and stack details", HttpStatus.INTERNAL_SERVER_ERROR)

        testApplication {
            application { configureMcpEndpoint({ patService }, { controller }) }
            val body = callMarkTasks("""{"isToDo":true,"taskIds":[7]}""").bodyAsText()

            assertThat(body).contains("internal error")
            assertThat(body).doesNotContain("database host and stack details")
        }
    }

    @Test
    fun markTasks_isListedInMcpToolCatalogWithRequiredArguments() {
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
            assertThat(body).contains("todo_mark_tasks")
            assertThat(body).contains("isToDo")
            assertThat(body).contains("taskIds")
            assertThat(body).contains("\"required\":[\"isToDo\",\"taskIds\"]")
        }
    }

    private suspend fun ApplicationTestBuilder.callMarkTasks(arguments: String) = client.post(MCP_ENDPOINT_PATH) {
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
              "params":{"name":"todo_mark_tasks","arguments":$arguments}
            }
            """.trimIndent(),
        )
    }
}
