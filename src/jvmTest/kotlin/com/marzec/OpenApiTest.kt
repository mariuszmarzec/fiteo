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

            assertThat(document).contains("/fiteo/api/1/exercises")
            assertThat(document).contains("/cheat/api/1/")
            assertThat(document).contains("/sse")
            assertThat(document).contains("/pat/api/1/tokens:")
            assertThat(document).contains("/pat/api/1/tokens/{id}:")
            assertThat(document).contains("List personal access tokens")
            assertThat(document).contains("Delete a personal access token")
            assertThat(document).contains("PersonalAccessTokenDto")
            assertThat(document).contains("tokenPrefix")
            assertThat(document).contains("lastUsedAt")
            assertThat(document).contains("Create a personal access token")
            assertThat(document).contains("Personal access tokens")
            assertThat(document).contains("CreatePersonalAccessTokenRequestDto")
            assertThat(document).contains("PersonalAccessTokenCreationDto")
            assertThat(document).contains("ErrorDto")
            assertThat(document).contains("The name is blank or the expiration policy is unsupported.")
            assertThat(document).contains("THREE_MONTHS")
            assertThat(document).contains("SIX_MONTHS")
            assertThat(document).contains("NEVER")
            assertThat(Regex("""(?m)^\s+["']?400["']?:""").containsMatchIn(document)).isTrue()
            assertThat(Regex("""(?m)^\s+["']?401["']?:""").containsMatchIn(document)).isTrue()

            // Swagger UI uses the runtime routing source. Keep these assertions here so the
            // route summary and request/response schemas are verified before snapshot generation.
            assertThat(document).doesNotContain("reszta endpointów /api/run_A")

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
        )
    }
}
