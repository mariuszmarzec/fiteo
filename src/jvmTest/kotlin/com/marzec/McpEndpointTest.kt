package com.marzec

import com.google.common.truth.Truth.assertThat
import com.marzec.mcp.MCP_ENDPOINT_PATH
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.*
import org.junit.Test

class McpEndpointTest {

    @Test
    fun initialize_returnsMcpServerCapabilities() {
        withDefaultMockTestApplication {
            val response = postMcp(
                """
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
            )

            assertThat(response.status).isEqualTo(HttpStatusCode.OK)
            val body = response.bodyAsText()
            assertThat(body).contains("\"jsonrpc\":\"2.0\"")
            assertThat(body).contains("\"protocolVersion\"")
            assertThat(body).contains("\"serverInfo\"")
            assertThat(body).contains("\"name\":\"fiteo\"")
        }
    }

    @Test
    fun endpoint_acceptsCustomHostWithoutOpenApiHostAllowlist() {
        withDefaultMockTestApplication {
            val response = client.post(MCP_ENDPOINT_PATH) {
                header(HttpHeaders.Host, "mcp.internal.example")
                header(HttpHeaders.Accept, "application/json, text/event-stream")
                contentType(ContentType.Application.Json)
                header("Mcp-Protocol-Version", "2025-11-25")
                setBody(
                    """
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
                )
            }

            assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        }
    }

    @Test
    fun toolsList_returnsAnEmptyToolRegistryInitially() {
        withDefaultMockTestApplication {
            val response = postMcp(
                """
                {
                  "jsonrpc": "2.0",
                  "id": 2,
                  "method": "tools/list",
                  "params": {}
                }
                """.trimIndent()
            )

            assertThat(response.status).isEqualTo(HttpStatusCode.OK)
            assertThat(response.bodyAsText()).contains("\"tools\":[]")
        }
    }

    @Test
    fun get_isNotSupportedByStatelessMcpTransport() {
        withDefaultMockTestApplication {
            val response = client.request(MCP_ENDPOINT_PATH) {
                method = HttpMethod.Get
                header(HttpHeaders.Accept, "application/json, text/event-stream")
            }

            assertThat(response.status).isEqualTo(HttpStatusCode.MethodNotAllowed)
        }
    }

    private suspend fun ApplicationTestBuilder.postMcp(body: String) =
        client.post(MCP_ENDPOINT_PATH) {
            header(HttpHeaders.Accept, "application/json, text/event-stream")
            contentType(ContentType.Application.Json)
            header("Mcp-Protocol-Version", "2025-11-25")
            setBody(body)
        }
}
