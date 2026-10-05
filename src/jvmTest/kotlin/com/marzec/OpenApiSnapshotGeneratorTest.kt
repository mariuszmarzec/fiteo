package com.marzec

import com.marzec.TestHelpersKt.withDefaultMockTestApplication
import io.ktor.http.ContentType
import org.junit.Test
import java.io.File

class OpenApiSnapshotGeneratorTest {

    @Test
    fun generateOpenApiSnapshot() {
        withDefaultMockTestApplication {
            startApplication()

            val generated = application.generateOpenApiSnapshot(ContentType.parse("application/yaml"))
            File("src/jvmMain/resources/openapi.yaml").writeText(generated)
        }
    }
}
