package com.marzec

import com.google.common.truth.Truth.assertThat
import com.marzec.cheatday.ApiPath as CheatDayApiPath
import com.marzec.fiteo.ApiPath as FiteoApiPath
import com.marzec.todo.ApiPath as TodoApiPath
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.Json
import org.junit.Test
import java.io.File

class OpenApiGenerationTest {

    @Test
    fun openApiContainsRoutesFromCompleteApplicationTree() {
        withDefaultMockTestApplication {
            startApplication()

            val response = client.get("/openapi")
            assertThat(response.status).isEqualTo(HttpStatusCode.OK)

            val document = Json.parseToJsonElement(response.bodyAsText()).jsonObject
            val paths = document["paths"]!!.jsonObject.keys

            assertThat(paths).containsAtLeast(
                TodoApiPath.TASKS,
                TodoApiPath.UPDATE_TASK,
                TodoApiPath.COPY_TASK,
                TodoApiPath.MARK_AS_TO_DO,
                TodoApiPath.LEAVE_SHARE,
                FiteoApiPath.EXERCISES,
                FiteoApiPath.TRAININGS,
                CheatDayApiPath.WEIGHTS,
                "/sse"
            )
        }
    }

    @Test
    fun openApiSnapshotMatchesApplicationTree() {
        withDefaultMockTestApplication {
            startApplication()

            val generated = application.generateOpenApiSnapshot(ContentType.parse("application/yaml"))
            val committed = File("src/jvmMain/resources/openapi.yaml").readText()

            assertThat(committed).isEqualTo(generated)
        }
    }
}
