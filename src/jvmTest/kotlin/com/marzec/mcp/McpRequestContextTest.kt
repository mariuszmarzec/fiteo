package com.marzec.mcp

import com.google.common.truth.Truth.assertThat
import com.marzec.Api
import com.marzec.exceptions.HttpException
import com.marzec.exceptions.HttpStatus
import com.marzec.fiteo.model.domain.User
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.Test
import kotlin.test.assertFailsWith

class McpRequestContextTest {

    @Test
    fun createRequest_usesVerifiedPatOwnerForControllerSession() {
        val context = McpRequestContext.fromVerifiedPatOwner(
            User(id = 42, email = "pat-owner@example.test"),
        )
        val arguments = buildJsonObject {
            put("id", 123)
            put("removeWithSubtasks", true)
        }

        val request = context.createRequest(
            data = Unit,
            arguments = arguments,
            parameters = mapOf(Api.Args.ARG_ID to "123"),
            queries = mapOf("removeWithSubtasks" to listOf("true")),
        )

        assertThat(request.sessions[Api.Args.ARG_USER_ID]).isEqualTo("42")
        assertThat(request.parameters[Api.Args.ARG_ID]).isEqualTo("123")
        assertThat(request.queries["removeWithSubtasks"]).containsExactly("true")
    }

    @Test
    fun createRequest_rejectsTopLevelCallerProvidedUserId() {
        val context = McpRequestContext.fromVerifiedPatOwner(
            User(id = 42, email = "pat-owner@example.test"),
        )
        val arguments = buildJsonObject {
            put("id", 123)
            put(Api.Args.ARG_USER_ID, 999)
        }

        val exception = assertFailsWith<HttpException> {
            context.createRequest(
                data = Unit,
                arguments = arguments,
                parameters = mapOf(Api.Args.ARG_ID to "123"),
            )
        }

        assertThat(exception.httpStatus).isEqualTo(HttpStatus.BAD_REQUEST)
    }

    @Test
    fun createRequest_rejectsUserIdInPathOrQueryContext() {
        val context = McpRequestContext.fromVerifiedPatOwner(
            User(id = 42, email = "pat-owner@example.test"),
        )
        val arguments = buildJsonObject { put("id", 123) }

        assertFailsWith<HttpException> {
            context.createRequest(
                data = Unit,
                arguments = arguments,
                parameters = mapOf(Api.Args.ARG_USER_ID to "999"),
            )
        }
        assertFailsWith<HttpException> {
            context.createRequest(
                data = Unit,
                arguments = arguments,
                queries = mapOf(Api.Args.ARG_USER_ID to listOf("999")),
            )
        }
    }

    @Test
    fun createRequest_preservesNestedShareUserIdAsDomainData() {
        val context = McpRequestContext.fromVerifiedPatOwner(
            User(id = 42, email = "pat-owner@example.test"),
        )
        val arguments = buildJsonObject {
            put(
                "task",
                buildJsonObject {
                    put(
                        "shares",
                        buildJsonArray {
                            add(buildJsonObject { put("userId", 99) })
                        },
                    )
                },
            )
        }

        val request = context.createRequest(
            data = arguments["task"]!!,
            arguments = arguments,
        )

        assertThat(request.sessions[Api.Args.ARG_USER_ID]).isEqualTo("42")
        assertThat(request.data.toString()).contains("\"userId\":99")
    }
}
