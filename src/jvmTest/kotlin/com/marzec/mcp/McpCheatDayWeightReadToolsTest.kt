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

class McpCheatDayWeightReadToolsTest {
    @Test
    fun listWeights_usesVerifiedPatOwnerAndReturnsOnlyTheirWeights() {
        val patService = mockk<PersonalAccessTokenService>()
        val todoController = mockk<ToDoApiController>(relaxed = true)
        val controller = mockk<CheatDayController>()
        val requestSlot = slot<HttpRequest<Unit>>()
        val weights = listOf(
            WeightDto(id = 17, value = 60.5f, date = "2026-10-27T10:00:00"),
            WeightDto(id = 18, value = 61.0f, date = "2026-10-28T10:00:00"),
        )
        every { patService.verify("valid-test-token") } returns User(id = 42, email = "owner@example.test")
        every { controller.getWeights(capture(requestSlot)) } returns DomainHttpResponse.Success(weights)

        testApplication {
            application {
                configureMcpEndpoint(
                    personalAccessTokenService = { patService },
                    todoController = { todoController },
                    cheatDayController = { controller },
                )
            }
            val response = callListWeights("""{}""")

            assertThat(response.status).isEqualTo(HttpStatusCode.OK)
            val body = response.bodyAsText()
            assertThat(body).contains("60.5")
            assertThat(body).contains("61.0")
            assertThat(body).contains("2026-10-28T10:00:00")
            assertThat(requestSlot.captured.sessions[Api.Args.ARG_USER_ID]).isEqualTo("42")
            verify(exactly = 1) { controller.getWeights(any()) }
        }
    }

    @Test
    fun listWeights_rejectsArgumentsAndCallerIdentityOverrideBeforeControllerDispatch() {
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
            val responses = listOf(
                callListWeights("""{"unexpected":true}""").bodyAsText(),
                callListWeights("""{"userId":999}""").bodyAsText(),
            )

            responses.forEach { assertThat(it).contains("\"isError\":true") }
            assertThat(responses[1]).contains("MCP tool arguments must not specify the caller userId")
            verify(exactly = 0) { controller.getWeights(any()) }
        }
    }

    @Test
    fun getWeight_usesVerifiedPatOwnerAndPassesPositiveId() {
        val patService = mockk<PersonalAccessTokenService>()
        val todoController = mockk<ToDoApiController>(relaxed = true)
        val controller = mockk<CheatDayController>()
        val requestSlot = slot<HttpRequest<Unit>>()
        every { patService.verify("valid-test-token") } returns User(id = 42, email = "owner@example.test")
        every { controller.getWeight(capture(requestSlot)) } returns DomainHttpResponse.Success(
            WeightDto(id = 17, value = 60.5f, date = "2026-10-27T10:00:00"),
        )

        testApplication {
            application {
                configureMcpEndpoint(
                    personalAccessTokenService = { patService },
                    todoController = { todoController },
                    cheatDayController = { controller },
                )
            }
            val response = callGetWeight("""{"id":17}""")

            assertThat(response.status).isEqualTo(HttpStatusCode.OK)
            assertThat(response.bodyAsText()).contains("60.5")
            assertThat(requestSlot.captured.sessions[Api.Args.ARG_USER_ID]).isEqualTo("42")
            assertThat(requestSlot.captured.parameters[Api.Args.ARG_ID]).isEqualTo("17")
            verify(exactly = 1) { controller.getWeight(any()) }
        }
    }

    @Test
    fun getWeight_rejectsMissingInvalidAndExtraArgumentsBeforeControllerDispatch() {
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
            val responses = listOf(
                callGetWeight("""{}""").bodyAsText(),
                callGetWeight("""{"id":"17"}""").bodyAsText(),
                callGetWeight("""{"id":0}""").bodyAsText(),
                callGetWeight("""{"id":17.5}""").bodyAsText(),
                callGetWeight("""{"id":17,"unexpected":true}""").bodyAsText(),
                callGetWeight("""{"id":17,"userId":999}""").bodyAsText(),
            )

            responses.forEach { assertThat(it).contains("\"isError\":true") }
            assertThat(responses.last()).contains("MCP tool arguments must not specify the caller userId")
            verify(exactly = 0) { controller.getWeight(any()) }
        }
    }

    @Test
    fun getWeight_mapsInaccessibleEntryToToolErrorForVerifiedOwner() {
        val patService = mockk<PersonalAccessTokenService>()
        val todoController = mockk<ToDoApiController>(relaxed = true)
        val controller = mockk<CheatDayController>()
        val requestSlot = slot<HttpRequest<Unit>>()
        every { patService.verify("valid-test-token") } returns User(id = 42, email = "owner@example.test")
        every { controller.getWeight(capture(requestSlot)) } returns DomainHttpResponse.Error<WeightDto>(
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
            val body = callGetWeight("""{"id":999}""").bodyAsText()

            assertThat(requestSlot.captured.sessions[Api.Args.ARG_USER_ID]).isEqualTo("42")
            assertThat(requestSlot.captured.parameters[Api.Args.ARG_ID]).isEqualTo("999")
            assertThat(body).contains("\"isError\":true")
            assertThat(body).contains("Weight not found")
            verify(exactly = 1) { controller.getWeight(any()) }
        }
    }

    @Test
    fun weightReadTools_areListedWithExplicitSchemas() {
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

            assertThat(body).contains("cheatday_list_weights")
            assertThat(body).contains("cheatday_get_weight")
            assertThat(body).contains("\"required\":[\"id\"]")
            assertThat(body).contains("\"minimum\":1")
        }
    }

    private suspend fun ApplicationTestBuilder.callListWeights(arguments: String) = client.post(MCP_ENDPOINT_PATH) {
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
              "params":{"name":"cheatday_list_weights","arguments":$arguments}
            }
            """.trimIndent(),
        )
    }

    private suspend fun ApplicationTestBuilder.callGetWeight(arguments: String) = client.post(MCP_ENDPOINT_PATH) {
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
              "params":{"name":"cheatday_get_weight","arguments":$arguments}
            }
            """.trimIndent(),
        )
    }
}
