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
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Test

class McpCheatDayWeightUpdateToolTest {
    @Test
    fun updateWeight_updatesOnlySuppliedValueAndUsesVerifiedPatOwner() {
        val patService = mockk<PersonalAccessTokenService>()
        val todoController = mockk<ToDoApiController>(relaxed = true)
        val controller = mockk<CheatDayController>()
        val requestSlot = slot<HttpRequest<Map<String, kotlinx.serialization.json.JsonElement?>>>()
        val updatedWeight = WeightDto(id = 17, value = 72.5f, date = "2026-10-27T10:00:00")
        every { patService.verify("valid-test-token") } returns User(id = 42, email = "owner@example.test")
        every { controller.updateWeight(capture(requestSlot)) } returns DomainHttpResponse.Success(updatedWeight)

        testApplication {
            application {
                configureMcpEndpoint(
                    personalAccessTokenService = { patService },
                    todoController = { todoController },
                    cheatDayController = { controller },
                )
            }
            val response = callUpdateWeight("""{"id":17,"value":72.5}""")

            assertThat(response.status).isEqualTo(HttpStatusCode.OK)
            val body = response.bodyAsText()
            assertThat(body).contains("72.5")
            assertThat(body).contains("2026-10-27T10:00:00")
            assertThat(requestSlot.captured.data.keys).containsExactly("value")
            assertThat(requestSlot.captured.data["value"]).isEqualTo(JsonPrimitive(72.5))
            assertThat(requestSlot.captured.sessions[Api.Args.ARG_USER_ID]).isEqualTo("42")
            assertThat(requestSlot.captured.parameters[Api.Args.ARG_ID]).isEqualTo("17")
            verify(exactly = 1) { controller.updateWeight(any()) }
        }
    }

    @Test
    fun updateWeight_updatesDateOnlyAndAllowsEmptyPartialUpdate() {
        val patService = mockk<PersonalAccessTokenService>()
        val todoController = mockk<ToDoApiController>(relaxed = true)
        val controller = mockk<CheatDayController>()
        val requests = mutableListOf<HttpRequest<Map<String, kotlinx.serialization.json.JsonElement?>>>()
        every { patService.verify("valid-test-token") } returns User(id = 42, email = "owner@example.test")
        every { controller.updateWeight(any()) } answers {
            val request = firstArg<HttpRequest<Map<String, kotlinx.serialization.json.JsonElement?>>>()
            requests += request
            DomainHttpResponse.Success(WeightDto(17, 70.0f, "2026-10-28T11:00:00"))
        }

        testApplication {
            application {
                configureMcpEndpoint(
                    personalAccessTokenService = { patService },
                    todoController = { todoController },
                    cheatDayController = { controller },
                )
            }
            val dateResponse = callUpdateWeight("""{"id":17,"date":"2026-10-28T11:00:00"}""")
            val emptyResponse = callUpdateWeight("""{"id":17}""")

            assertThat(dateResponse.status).isEqualTo(HttpStatusCode.OK)
            assertThat(emptyResponse.status).isEqualTo(HttpStatusCode.OK)
            assertThat(requests).hasSize(2)
            assertThat(requests[0].data.keys).containsExactly("date")
            assertThat(requests[0].data["date"]).isEqualTo(JsonPrimitive("2026-10-28T11:00:00"))
            assertThat(requests[1].data).isEmpty()
            assertThat(requests.all { it.sessions[Api.Args.ARG_USER_ID] == "42" }).isTrue()
            verify(exactly = 2) { controller.updateWeight(any()) }
        }
    }

    @Test
    fun updateWeight_rejectsInvalidIdsPayloadTypesDatesAndUnknownFieldsBeforeDispatch() {
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
                """{"id":"17","value":72.5}""",
                """{"id":0,"value":72.5}""",
                """{"id":17.5,"value":72.5}""",
                """{"id":17,"value":"72.5"}""",
                """{"id":17,"value":1e100}""",
                """{"id":17,"date":12}""",
                """{"id":17,"date":"not-a-date"}""",
                """{"id":17,"date":"2026-10-28T11:00:00Z"}""",
                """{"id":17,"userId":999,"value":72.5}""",
                """{"id":17,"unknown":true}""",
            )
            val responses = inputs.map { callUpdateWeight(it).bodyAsText() }

            responses.forEach { assertThat(it).contains("\"isError\":true") }
            verify(exactly = 0) { controller.updateWeight(any()) }
        }
    }

    @Test
    fun updateWeight_mapsInaccessibleEntryToToolErrorForVerifiedOwner() {
        val patService = mockk<PersonalAccessTokenService>()
        val todoController = mockk<ToDoApiController>(relaxed = true)
        val controller = mockk<CheatDayController>()
        val requestSlot = slot<HttpRequest<Map<String, kotlinx.serialization.json.JsonElement?>>>()
        every { patService.verify("valid-test-token") } returns User(id = 42, email = "owner@example.test")
        every { controller.updateWeight(capture(requestSlot)) } returns DomainHttpResponse.Error<WeightDto>(
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
            val body = callUpdateWeight("""{"id":999,"value":75.0}""").bodyAsText()

            assertThat(requestSlot.captured.sessions[Api.Args.ARG_USER_ID]).isEqualTo("42")
            assertThat(requestSlot.captured.parameters[Api.Args.ARG_ID]).isEqualTo("999")
            assertThat(body).contains("\"isError\":true")
            assertThat(body).contains("Weight not found")
            verify(exactly = 1) { controller.updateWeight(any()) }
        }
    }

    @Test
    fun updateWeight_isAdvertisedWithExplicitSchema() {
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

            assertThat(body).contains("cheatday_update_weight")
            assertThat(body).contains("\"required\":[\"id\"]")
            assertThat(body).contains("\"minimum\":1")
            assertThat(body).contains("\"value\"")
            assertThat(body).contains("\"date\"")
        }
    }

    private suspend fun ApplicationTestBuilder.callUpdateWeight(arguments: String) = client.post(MCP_ENDPOINT_PATH) {
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
              "params":{"name":"cheatday_update_weight","arguments":$arguments}
            }
            """.trimIndent(),
        )
    }
}
