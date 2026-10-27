package com.marzec

import com.google.common.truth.Truth.assertThat
import com.marzec.Api.Headers
import com.marzec.fiteo.PatApiPath
import com.marzec.fiteo.model.dto.CreatePersonalAccessTokenRequestDto
import com.marzec.fiteo.model.dto.ErrorDto
import com.marzec.fiteo.model.dto.PersonalAccessTokenCreationDto
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.jsonObject
import org.junit.After
import org.junit.Test
import org.koin.core.context.GlobalContext
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PersonalAccessTokenApiTest {

    @Test
    fun `create token requires authentication`() {
        withDefaultMockTestApplication {
            val response = client.post(PatApiPath.TOKENS) {
                setBodyJson(CreatePersonalAccessTokenRequestDto(name = "CLI", expiresIn = "NEVER"))
            }

            assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
        }
    }

    @Test
    fun `create token returns the secret and expected metadata for each expiration option`() {
        withDefaultMockTestApplication {
            authToken = registerAndLogin()

            listOf(
                Triple("Three months", "THREE_MONTHS", true),
                Triple("Six months", "SIX_MONTHS", true),
                Triple("Never", "NEVER", false)
            ).forEach { (name, expiresIn, shouldExpire) ->
                val response = client.post(PatApiPath.TOKENS) {
                    header(Headers.AUTHORIZATION, authToken!!)
                    setBodyJson(CreatePersonalAccessTokenRequestDto(name = name, expiresIn = expiresIn))
                }

                assertThat(response.status).isEqualTo(HttpStatusCode.OK)
                val bodyText = response.bodyAsText()
                val creation = json.decodeFromString<PersonalAccessTokenCreationDto>(bodyText)

                assertEquals(name, creation.name)
                assertTrue(creation.token.matches(Regex("^[0-9a-f]{64}$")))
                assertTrue(creation.prefix.matches(Regex("^[0-9a-f]{8}$")))
                assertEquals(shouldExpire, creation.expiresAt != null)
                assertNotNull(creation.createdAt)

                val responseFields = json.parseToJsonElement(bodyText).jsonObject.keys
                assertEquals(
                    setOf("id", "name", "token", "prefix", "createdAt", "expiresAt"),
                    responseFields
                )
                assertThat(responseFields).doesNotContain("tokenHash")
            }
        }
    }

    @Test
    fun `create token rejects an empty or whitespace-only name`() {
        withDefaultMockTestApplication {
            authToken = registerAndLogin()

            val response = client.post(PatApiPath.TOKENS) {
                header(Headers.AUTHORIZATION, authToken!!)
                setBodyJson(CreatePersonalAccessTokenRequestDto(name = "  \t ", expiresIn = "NEVER"))
            }

            assertThat(response.status).isEqualTo(HttpStatusCode.BadRequest)
            assertEquals("Empty name", json.decodeFromString<ErrorDto>(response.bodyAsText()).reason)
        }
    }

    @Test
    fun `create token rejects an unknown expiration option`() {
        withDefaultMockTestApplication {
            authToken = registerAndLogin()

            val response = client.post(PatApiPath.TOKENS) {
                header(Headers.AUTHORIZATION, authToken!!)
                setBodyJson(CreatePersonalAccessTokenRequestDto(name = "CLI", expiresIn = "FOREVER"))
            }

            assertThat(response.status).isEqualTo(HttpStatusCode.BadRequest)
            assertEquals("Unknown expiresIn", json.decodeFromString<ErrorDto>(response.bodyAsText()).reason)
        }
    }

    @After
    fun tearDown() {
        GlobalContext.stopKoin()
    }
}
