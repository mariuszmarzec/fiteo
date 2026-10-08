package com.marzec

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertTrue

class OpenApiRoutingTest {

    @Test
    fun `generated openapi spec contains todo and cheatday routes`() {
        setupDb()
        testApplication {
            application {
                module()
            }
            // swaggerUI serves the raw OpenAPI spec (JSON/YAML) at its remotePath;
            // the openAPI plugin serves only the swagger-codegen HTML UI.
            val response = client.get("/swagger/documentation.yaml")
            println("STATUS: ${response.status}")
            println("CONTENT-TYPE: ${response.headers["Content-Type"]}")
            val body = response.bodyAsText()
            println("BODY LENGTH: ${body.length}")
            println("BODY PREVIEW: ${body.take(500)}")
            println("CONTAINS_TODO_TASKS: ${body.contains("/todo/api/1/tasks")}")
            println("CONTAINS_TODO_COPY: ${body.contains("/todo/api/1/tasks/{id}/copy")}")
            println("CONTAINS_CHEATDAY: ${body.contains("/cheat/api/1/weights")}")
            println("CONTAINS_FITEO: ${body.contains("/fiteo/api/1/users")}")
            java.io.File("/tmp/openapi_body.txt").writeText(body)
            println("=== END ===")
            assertTrue(body.contains("/todo/api/1/tasks"), "TODO route missing from OpenAPI spec:\n$body")
            assertTrue(body.contains("/cheat/api/1/weights"), "CheatDay route missing from OpenAPI spec:\n$body")
            assertTrue(body.contains("/fiteo/api/1/users"), "Fiteo route missing from OpenAPI spec:\n$body")
        }
    }
}