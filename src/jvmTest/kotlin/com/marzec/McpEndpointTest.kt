package com.marzec

import com.google.common.truth.Truth.assertThat
import com.marzec.Api.Headers
import com.marzec.database.DbSettings
import com.marzec.database.PersonalAccessTokenTable
import com.marzec.fiteo.PatApiPath
import com.marzec.fiteo.model.dto.CreatePersonalAccessTokenRequestDto
import com.marzec.fiteo.model.dto.PersonalAccessTokenCreationDto
import com.marzec.mcp.MCP_ENDPOINT_PATH
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.*
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneOffset

class McpEndpointTest {

    @Test
    fun initialize_requiresPatBeforeProtocolHandling() {
        withDefaultMockTestApplication {
            val response = postMcp(
                body = "not valid JSON"
            )

            assertUnauthorizedWithoutCredentialDetails(response.status, response.bodyAsText())
            assertThat(response.headers[HttpHeaders.WWWAuthenticate]).isEqualTo("Bearer")
        }
    }

    @Test
    fun toolsList_requiresPat() {
        withDefaultMockTestApplication {
            val response = postMcp(
                body = """
                    {
                      "jsonrpc": "2.0",
                      "id": 2,
                      "method": "tools/list",
                      "params": {}
                    }
                """.trimIndent()
            )

            assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
            assertThat(response.bodyAsText()).isEmpty()
        }
    }

    @Test
    fun malformedAuthorizationHeaders_areRejectedUniformly() {
        withDefaultMockTestApplication {
            val invalidAuthorizationHeaders = listOf(
                "Basic secret",
                "Bearer",
                "Bearer  secret",
                "Bearer secret extra",
                "Bearer secret "
            )

            for (authorization in invalidAuthorizationHeaders) {
                val response = postMcp(
                    body = "not valid JSON",
                    authorization = authorization
                )

                assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
                assertThat(response.bodyAsText()).isEmpty()
                assertThat(response.headers[HttpHeaders.WWWAuthenticate]).isEqualTo("Bearer")
            }
        }
    }

    @Test
    fun invalidPat_isRejectedUniformly() {
        withDefaultMockTestApplication {
            val invalidPat = "0".repeat(64)
            val response = postMcp(
                body = initializeRequest,
                authorization = "Bearer $invalidPat"
            )

            assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
            assertThat(response.bodyAsText()).isEmpty()
            assertThat(response.bodyAsText()).doesNotContain(invalidPat)
        }
    }

    @Test
    fun expiredPat_isRejectedUniformly() {
        withDefaultMockTestApplication {
            val pat = createPat(expiresIn = "THREE_MONTHS")
            transaction(DbSettings.database) {
                PersonalAccessTokenTable.update(
                    where = { PersonalAccessTokenTable.id eq pat.id }
                ) {
                    it[expiresAt] = LocalDateTime.now(ZoneOffset.UTC).minusDays(1)
                }
            }

            val response = postMcp(
                body = initializeRequest,
                authorization = "Bearer ${pat.token}"
            )

            assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
            assertThat(response.bodyAsText()).isEmpty()
        }
    }

    @Test
    fun deletedPat_isRejectedUniformly() {
        withDefaultMockTestApplication {
            val pat = createPat()
            val deleteResponse = client.delete(
                PatApiPath.TOKEN_BY_ID.replace("{id}", pat.id.toString())
            ) {
                header(Headers.AUTHORIZATION, authToken!!)
            }
            assertThat(deleteResponse.status).isEqualTo(HttpStatusCode.OK)

            val response = postMcp(
                body = initializeRequest,
                authorization = "Bearer ${pat.token}"
            )

            assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
            assertThat(response.bodyAsText()).isEmpty()
        }
    }

    @Test
    fun initialize_acceptsValidPat() {
        withDefaultMockTestApplication {
            val pat = createPat()
            val response = postMcp(initializeRequest, authorization = "Bearer ${pat.token}")

            assertThat(response.status).isEqualTo(HttpStatusCode.OK)
            val body = response.bodyAsText()
            assertThat(body).contains("\"jsonrpc\":\"2.0\"")
            assertThat(body).contains("\"protocolVersion\"")
            assertThat(body).contains("\"serverInfo\"")
            assertThat(body).contains("\"name\":\"fiteo\"")
        }
    }

    @Test
    fun toolsList_returnsAnEmptyToolRegistryWithValidPat() {
        withDefaultMockTestApplication {
            val pat = createPat()
            val response = postMcp(
                body = """
                    {
                      "jsonrpc": "2.0",
                      "id": 2,
                      "method": "tools/list",
                      "params": {}
                    }
                """.trimIndent(),
                authorization = "Bearer ${pat.token}"
            )

            assertThat(response.status).isEqualTo(HttpStatusCode.OK)
            assertThat(response.bodyAsText()).contains("\"tools\":[]")
        }
    }

    @Test
    fun get_isNotSupportedByStatelessMcpTransportForValidPat() {
        withDefaultMockTestApplication {
            val pat = createPat()
            val response = client.request(MCP_ENDPOINT_PATH) {
                method = HttpMethod.Get
                header(HttpHeaders.Accept, "application/json, text/event-stream")
                header(HttpHeaders.Authorization, "Bearer ${pat.token}")
            }

            assertThat(response.status).isEqualTo(HttpStatusCode.MethodNotAllowed)
        }
    }

    private suspend fun ApplicationTestBuilder.createPat(
        expiresIn: String = "NEVER"
    ): PersonalAccessTokenCreationDto {
        authToken = registerAndLogin()
        return postWithAuth(
            PatApiPath.TOKENS,
            CreatePersonalAccessTokenRequestDto(name = "MCP test token", expiresIn = expiresIn)
        )
    }

    private suspend fun ApplicationTestBuilder.postMcp(
        body: String,
        authorization: String? = null
    ) = client.post(MCP_ENDPOINT_PATH) {
        header(HttpHeaders.Accept, "application/json, text/event-stream")
        contentType(ContentType.Application.Json)
        header("Mcp-Protocol-Version", "2025-11-25")
        authorization?.let { header(HttpHeaders.Authorization, it) }
        setBody(body)
    }

    private fun assertUnauthorizedWithoutCredentialDetails(status: HttpStatusCode, body: String) {
        assertThat(status).isEqualTo(HttpStatusCode.Unauthorized)
        assertThat(body).isEmpty()
    }

    private companion object {
        val initializeRequest = """
            {
              "jsonrpc": "2.0",
              "id": 1,
              "method": "initialize",
              "params": {
                "protocolVersion": "2025-11-25",
                "capabilities": {},
                "clientInfo": {
                  "name": "fiteo-mcp-test",
                  "version": "1.0.0"
                }
              }
            }
        """.trimIndent()
    }
}
