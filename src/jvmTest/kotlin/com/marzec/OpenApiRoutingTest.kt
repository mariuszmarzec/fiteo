package com.marzec

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OpenApiRoutingTest {

    @Test
    fun `generated openapi spec contains the complete application routing tree`() {
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
            java.io.File("/tmp/openapi_body.txt").writeText(body)
            println("=== END ===")

            assertTrue(response.status.value in 200..299, "OpenAPI spec not served: ${response.status}")
            assertTrue(body.contains("\"openapi\""), "Spec is not an OpenAPI document:\n$body")

            // Regression coverage: every real TODO route must stay in the generated spec.
            val requiredTodoRoutes = listOf(
                "/todo/api/1/tasks",
                "/todo/api/1/tasks/{id}",
                "/todo/api/1/tasks/{id}/copy",
                "/todo/api/1/tasks/mark-as-to-do",
                "/todo/api/1/tasks/leave-share"
            )
            requiredTodoRoutes.forEach { route ->
                assertTrue(body.contains(route), "Required TODO route missing from OpenAPI spec: $route\n$body")
            }

            // Other APIs registered by Application.module() must also be present.
            val requiredOtherRoutes = listOf(
                "/fiteo/api/1/users",
                "/fiteo/api/1/exercises",
                "/cheat/api/1/weights",
                "/sse"
            )
            requiredOtherRoutes.forEach { route ->
                assertTrue(body.contains(route), "Required route missing from OpenAPI spec: $route\n<body")
            }

            // Intentionally excluded resources must NOT appear in the spec.
            listOf("/", "/fiteo.js", "/scripts", "/test").forEach { route ->
                assertTrue(!body.contains("\"$route\""), "Excluded route leaked into OpenAPI spec: $route\n<body")
            }
        }
    }
}