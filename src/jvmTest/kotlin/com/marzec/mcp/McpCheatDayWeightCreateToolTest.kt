package com.marzec.mcp

import com.google.common.truth.Truth.assertThat
import com.marzec.Api
import com.marzec.cheatday.CheatDayController
import com.marzec.cheatday.dto.PutWeightDto
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

class McpCheatDayWeightCreateToolTest {
    @Test
    fun createWeight_usesVerifiedPatOwnerAndReturnsCreatedWeight() {
        val patService = mockk<PersonalAccessTokenService>()
        val todoController = mockk<ToDoApiController>(relaxed = true)
        val controller = mockk<CheatDayController>()
        val requestSlot = slot<HttpRequest<PutWeightDto>>()
        val createdWeight = WeightDto(id = 88, value = 72.5f, date = "2026-10-29T08:30:00")
        every { patService.verify("valid-test-token") } returns User(id = 42, email = "owner@example.test")
        every { controller.putWeight(capture(requestSlot)) } returns DomainHttpResponse.Success(createdWeight)

        testApplication {
            application {
                configureMcpEndpoint(
                    personalAccessTokenService = { patService },
                    todoController = { todoController },
                    cheatDayController = { controller },
                )
            }
            val response = callCreateWeight("""{"value":72.5,"date":"2026-10-29T08:30:00"}""")

            assertThat(response.status).isEqualTo(HttpStatusCode.OK)
            val body = response.bodyAsText()
            assertThat(body).contains("\"id\":88")
            assertThat(body).contains("72.5")
            assertThat(body).contains("2026-10-29T08:30:00")
            assertThat(requestSlot.captured.data).isEqualTo(
                PutWeightDto(value = 72.5f, date = "2026-10-29T08:30:00"),
            )
            assertThat(requestSlot.captured.sessions[Api.Args.ARG_USER_ID]).isEqualTo("42")
            verify(exactly = 1) { controller.putWeight(any()) }
        }
    }

    @Test
    fun createWeight_rejectsMissingWrongTypedInvalidDateAndExtraArgumentsBeforeControllerDispatch() {
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
                callCreateWeight("""{}""").bodyAsText(),
                callCreateWeight("""{"value":"72.5","date":"2026-10-29T08:30:00"}""").bodyAsText(),
                callCreateWeight("""{"value":72.5,"date":"not-a-date"}""").bodyAsText(),
                callCreateWeight("""{"value":72.5,"date":"2026-10-29T08:30:00Z"}""").bodyAsText(),
                callCreateWeight("""{"value":72.5,"date":"2026-10-29T08:30:00","userId":999}""").bodyAsText(),
                callCreateWeight("""{"value":72.5,"date":"2026-10-29T08:30:00","unexpected":true}""").bodyAsText(),
            )

            responses.forEach { assertThat(it).contains("\"isError\":true") }
            assertThat(responses[4]).contains("Invalid cheatday_create_weight arguments")
            verify(exactly = 0) { controller.putWeight(any()) }
        }
    }

    @Test
    fun createWeight_mapsControllerErrorsToToolErrors() {
        val patService = mockk<PersonalAccessTokenService>()
        val todoController = mockk<ToDoApiController>(relaxed = true)
        val controller = mockk<CheatDayController>()
        every { patService.verify("valid-test-token") } returns User(id = 42, email = "owner@example.test")
        every { controller.putWeight(any()) } returns DomainHttpResponse.Error<WeightDto>(
            data = ErrorDto("Invalid weight entry"),
            httpStatusCode = 400,
        )

        testApplication {
            application {
                configureMcpEndpoint(
                    personalAccessTokenService = { patService },
                    todoController = { todoController },
                    cheatDayController = { controller },
                )
            }
            val body = callCreateWeight("""{"value":72.5,"date":"2026-10-29T08:30:00"}""").bodyAsText()

            assertThat(body).contains("\"isError\":true")
            assertThat(body).contains("Invalid weight entry")
            verify(exactly = 1) { controller.putWeight(any()) }
        }
    }

    @Test
    fun createWeight_isAdvertisedWithExplicitInputSchema() {
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

            assertThat(body).contains("cheatday_create_weight")
            assertThat(body).contains("\"type\":\"number\"")
            assertThat(body).contains("\"required\":[\"value\",\"date\"]")
        }
    }

    private suspend fun ApplicationTestBuilder.callCreateWeight(arguments: String) = client.post(MCP_ENDPOINT_PATH) {
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
              "params":{"name":"cheatday_create_weight","arguments":$arguments}
            }
            """.trimIndent(),
        )
    }
}
