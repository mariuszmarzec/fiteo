package com.marzec.mcp

import com.google.common.truth.Truth.assertThat
import com.marzec.Api
import com.marzec.cheatday.CheatDayController
import com.marzec.cheatday.dto.WeightDto
import com.marzec.fiteo.model.domain.User
import com.marzec.fiteo.model.dto.ErrorDto
import com.marzec.fiteo.model.http.HttpRequest
import com.marzec.fiteo.model.http.HttpResponse as DomainHttpResponse
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
import io.mockk.slot
import io.mockk.verify
import org.junit.Test

class McpCheatDayWeightDeleteToolTest {
    @Test
    fun deleteWeight_usesVerifiedPatOwnerAndReturnsRemovedWeight() {
        val patService = mockk<PersonalAccessTokenService>()
        val todoController = mockk<ToDoApiController>(relaxed = true)
        val controller = mockk<CheatDayController>()
        val requestSlot = slot<HttpRequest<Unit>>()
        val removedWeight = WeightDto(id = 17, value = 61.5f, date = "2026-10-27T10:00:00")
        every { patService.verify("valid-test-token") } returns User(id = 42, email = "owner@example.test")
        every { controller.removeWeight(capture(requestSlot)) } returns DomainHttpResponse.Success(removedWeight)

        testApplication {
            application {
                configureMcpEndpoint(
                    personalAccessTokenService = { patService },
                    todoController = { todoController },
                    cheatDayController = { controller },
                )
            }
            val response = callDeleteWeight("""{"id":17}""")

            assertThat(response.status).isEqualTo(HttpStatusCode.OK)
            val body = response.bodyAsText()
            assertThat(body).contains("61.5")
            assertThat(body).contains("2026-10-27T10:00:00")
            assertThat(requestSlot.captured.sessions[Api.Args.ARG_USER_ID]).isEqualTo("42")
            assertThat(requestSlot.captured.parameters[Api.Args.ARG_ID]).isEqualTo("17")
            verify(exactly = 1) { controller.removeWeight(any()) }
        }
    }

    @Test
    fun deleteWeight_rejectsMissingInvalidExtraAndCallerIdentityArgumentsBeforeDispatch() {
        val patService = mockk<PersonalAccessTokenService>()
        val todoController = mockk<ToDoApiController>(relaxed = true)
        val controller = mockk<CheatDayController>(relaxed = true)
        every { patService.verify("valid-test-token") } returns User(id = 42, email = "owner@example.test")

        testApplication {
            application {
                configureMcpEndpoint(
                    personalAccessTokenService = { patService },
                    todoController = { todoController },
                    cheatDayController = { controller },
                )
            }
            val inputs = listOf(
                """{}""",
                """{"id":"17"}""",
                """{"id":0}""",
                """{"id":-1}""",
                """{"id":17.5}""",
                """{"id":17,"userId":999}""",
                """{"id":17,"unexpected":true}""",
            )
            val responses = inputs.map { callDeleteWeight(it).bodyAsText() }

            responses.forEach { assertThat(it).contains("\"isError\":true") }
            verify(exactly = 0) { controller.removeWeight(any()) }
        }
    }

    @Test
    fun deleteWeight_mapsMissingOrInaccessibleEntryToToolErrorForVerifiedOwner() {
        val patService = mockk<PersonalAccessTokenService>()
        val todoController = mockk<ToDoApiController>(relaxed = true)
        val controller = mockk<CheatDayController>()
        val requestSlot = slot<HttpRequest<Unit>>()
        every { patService.verify("valid-test-token") } returns User(id = 42, email = "owner@example.test")
        every { controller.removeWeight(capture(requestSlot)) } returns DomainHttpResponse.Error<WeightDto>(
            data = ErrorDto("Weight not found"),
            httpStatusCode = 404,
        )

        testApplication {
            application {
                configureMcpEndpoint(
                    personalAccessTokenService = { patService },
                    todoController = { todoController },
                    cheatDayController = { controller },
                )
            }
            val body = callDeleteWeight("""{"id":999}""").bodyAsText()

            assertThat(requestSlot.captured.sessions[Api.Args.ARG_USER_ID]).isEqualTo("42")
            assertThat(requestSlot.captured.parameters[Api.Args.ARG_ID]).isEqualTo("999")
            assertThat(body).contains("\"isError\":true")
            assertThat(body).contains("Weight not found")
            verify(exactly = 1) { controller.removeWeight(any()) }
        }
    }

    @Test
    fun deleteWeight_isAdvertisedAsDestructiveWithExplicitSchema() {
        val patService = mockk<PersonalAccessTokenService>()
        val todoController = mockk<ToDoApiController>(relaxed = true)
        val controller = mockk<CheatDayController>(relaxed = true)
        every { patService.verify("valid-test-token") } returns User(id = 42, email = "owner@example.test")

        testApplication {
            application {
                configureMcpEndpoint(
                    personalAccessTokenService = { patService },
                    todoController = { todoController },
                    cheatDayController = { controller },
                )
            }
            val response = client.post(MCP_ENDPOINT_PATH) {
                header(HttpHeaders.Accept, "application/json, text/event-stream")
                contentType(ContentType.Application.Json)
                header("Mcp-Protocol-Version", "2025-11-25")
                header(HttpHeaders.Authorization, "Bearer valid-test-token")
                setBody("""{"jsonrpc":"2.0","id":2,"method":"tools/list"}""")
            }
            val body = response.bodyAsText()

            assertThat(body).contains("cheatday_delete_weight")
            assertThat(body).contains("Destructive operation")
            assertThat(body).contains("\"required\":[\"id\"]")
            assertThat(body).contains("\"minimum\":1")
        }
    }

    private suspend fun ApplicationTestBuilder.callDeleteWeight(arguments: String) = client.post(MCP_ENDPOINT_PATH) {
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
              "params":{"name":"cheatday_delete_weight","arguments":$arguments}
            }
            """.trimIndent(),
        )
    }
}
