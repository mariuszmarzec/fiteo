package com.marzec

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import java.io.File

fun main(args: Array<String>) {
    val outputPath = args.getOrNull(0) ?: "src/jvmMain/resources/openapi.yaml"
    generateOpenApiSpec(File(outputPath))
}

/**
 * Boots the Ktor application (with the test database) and writes the generated
 * OpenAPI specification to [outputFile].
 *
 * The `swaggerUI` plugin serves the raw spec (JSON by default) at its `remotePath`
 * sub-path, while the `openAPI` plugin only serves the swagger-codegen HTML UI.
 */
fun generateOpenApiSpec(outputFile: File) {
    setupDb()
    testApplication {
        application {
            module()
        }
val response = client.get("/swagger/documentation.yaml")
            check(response.status.value in 200..299) {
                "Failed to generate OpenAPI spec: HTTP ${response.status}"
            }
            val contentType = response.headers[HttpHeaders.ContentType]
        check(contentType?.contains("json") == true || contentType?.contains("yaml") == true) {
            "Unexpected content-type $contentType for OpenAPI spec"
        }
        val spec = response.bodyAsText()
        outputFile.parentFile.mkdirs()
        outputFile.writeText(spec)
        println("OpenAPI spec written to ${outputFile.absolutePath} (${spec.length} chars)")
    }
}