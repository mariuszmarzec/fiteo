package com.marzec

import com.google.common.truth.Truth.assertThat
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import org.junit.Test
import java.io.File

class OpenApiTest {

    @Test
    fun generatedOpenApi_containsAllRoutesFromApplicationRoutingTree() {
        withDefaultMockTestApplication {
            val response = client.get("/swagger/openapi.yaml")

            assertThat(response.status).isEqualTo(HttpStatusCode.OK)

            val document = response.bodyAsText()

            expectedTodoRoutes.forEach { route ->
                assertThat(document).contains(route)
            }

            // Verify that generation is not accidentally scoped to the Fiteo package.
            assertThat(document).contains("/fiteo/exercises:")
            assertThat(document).contains("/cheat/api/1/")
            assertThat(document).contains("/sse")

            System.getProperty(SNAPSHOT_OUTPUT_PROPERTY)
                ?.takeIf { it.isNotBlank() }
                ?.let { path ->
                    val output = File(path)
                    output.parentFile.mkdirs()
                    output.writeText(document)
                }
        }
    }

    private companion object {
        const val SNAPSHOT_OUTPUT_PROPERTY = "openapi.snapshot.output"

        val expectedTodoRoutes = listOf(
            "/todo/api/1/tasks:",
            "/todo/api/1/tasks/{id}:",
            "/todo/api/1/tasks/{id}/copy:",
            "/todo/api/1/tasks/mark-as-to-do:",
            "/todo/api/1/tasks/leave-share:",
        )
    }
}
