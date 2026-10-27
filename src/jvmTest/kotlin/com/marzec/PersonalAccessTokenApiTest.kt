package com.marzec

import com.google.common.truth.Truth.assertThat
import com.marzec.Api.Headers
import com.marzec.fiteo.PatApiPath
import com.marzec.fiteo.model.dto.CreatePersonalAccessTokenRequestDto
import com.marzec.fiteo.model.dto.ErrorDto
import com.marzec.fiteo.model.dto.LoginRequestDto
import com.marzec.fiteo.model.dto.PersonalAccessTokenCreationDto
import com.marzec.fiteo.model.dto.PersonalAccessTokenDto
import com.marzec.fiteo.model.dto.RegisterRequestDto
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.*
import kotlinx.serialization.decodeFromString
import org.junit.After
import org.junit.Test
import org.koin.core.context.GlobalContext

class PersonalAccessTokenApiTest {

    @Test
    fun createPersonalAccessToken_requiresAuthentication() {
        withDefaultMockTestApplication {
            val response = client.post(PatApiPath.TOKENS) {
                setBodyJson(createPersonalAccessTokenRequest)
            }

            assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
        }
    }

    @Test
    fun createPersonalAccessToken_threeMonths() {
        assertCreatedToken(expiresIn = "THREE_MONTHS", shouldExpire = true)
    }

    @Test
    fun createPersonalAccessToken_sixMonths() {
        assertCreatedToken(expiresIn = "SIX_MONTHS", shouldExpire = true)
    }

    @Test
    fun createPersonalAccessToken_neverExpires() {
        assertCreatedToken(expiresIn = "NEVER", shouldExpire = false)
    }

    @Test
    fun createPersonalAccessToken_rejectsBlankName() {
        testPostEndpoint(
            uri = PatApiPath.TOKENS,
            dto = createPersonalAccessTokenRequest.copy(name = "  \t "),
            status = HttpStatusCode.BadRequest,
            responseDto = ErrorDto("Empty name"),
            authorize = ApplicationTestBuilder::registerAndLogin
        )
    }

    @Test
    fun createPersonalAccessToken_rejectsUnknownExpiration() {
        testPostEndpoint(
            uri = PatApiPath.TOKENS,
            dto = createPersonalAccessTokenRequest.copy(expiresIn = "FOREVER"),
            status = HttpStatusCode.BadRequest,
            responseDto = ErrorDto("Unknown expiresIn"),
            authorize = ApplicationTestBuilder::registerAndLogin
        )
    }

    @Test
    fun listPersonalAccessTokens_requiresAuthentication() {
        withDefaultMockTestApplication {
            val response = client.get(PatApiPath.TOKENS)

            assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
        }
    }

    @Test
    fun listPersonalAccessTokens_returnsOnlyMetadataForAuthenticatedUser() {
        withDefaultMockTestApplication {
            val userAuth = registerAndLogin()
            authToken = userAuth
            val firstToken = createToken(name = "CLI", expiresIn = "NEVER")
            val secondToken = createToken(name = "Mobile", expiresIn = "THREE_MONTHS")

            val otherEmail = "other-user@mail.com"
            register(
                RegisterRequestDto(
                    email = otherEmail,
                    password = "1234567890",
                    repeatedPassword = "1234567890"
                )
            )
            authToken = login(LoginRequestDto(email = otherEmail, password = "1234567890"))
            val otherUsersToken = createToken(name = "Other user's token", expiresIn = "NEVER")

            val response = client.get(PatApiPath.TOKENS) {
                header(Headers.AUTHORIZATION, userAuth)
            }

            assertThat(response.status).isEqualTo(HttpStatusCode.OK)
            val body = response.bodyAsText()
            val tokens = json.decodeFromString<List<PersonalAccessTokenDto>>(body)

            assertThat(tokens).hasSize(2)
            assertThat(tokens.map { it.id }).containsExactly(firstToken.id, secondToken.id)
            assertThat(tokens.map { it.name }).containsExactly("CLI", "Mobile")
            assertThat(tokens.map { it.tokenPrefix }).containsExactly(firstToken.prefix, secondToken.prefix)
            tokens.forEach { token ->
                assertThat(token.tokenPrefix).matches("^[0-9a-f]{8}$")
                assertThat(token.createdAt).matches(DATE_TIME_PATTERN)
            }
            val listedFirstToken = tokens.first { it.id == firstToken.id }
            val listedSecondToken = tokens.first { it.id == secondToken.id }
            assertThat(listedFirstToken.expiresAt).isNull()
            assertThat(listedSecondToken.expiresAt).isNotNull()
            assertThat(listedSecondToken.expiresAt!!).matches(DATE_TIME_PATTERN)
            assertThat(tokens.all { it.lastUsedAt == null }).isTrue()

            assertThat(body).doesNotContain(firstToken.token)
            assertThat(body).doesNotContain(secondToken.token)
            assertThat(body).doesNotContain(otherUsersToken.token)
            assertThat(body).doesNotContain("Other user's token")
            assertThat(body).doesNotContain("tokenHash")
            assertThat(body).doesNotContain("\"token\":")
        }
    }

    private suspend fun ApplicationTestBuilder.createToken(
        name: String,
        expiresIn: String
    ): PersonalAccessTokenCreationDto = postWithAuth<CreatePersonalAccessTokenRequestDto, PersonalAccessTokenCreationDto>(
        PatApiPath.TOKENS,
        CreatePersonalAccessTokenRequestDto(name = name, expiresIn = expiresIn)
    )

    private fun assertCreatedToken(expiresIn: String, shouldExpire: Boolean) {
        testPostEndpoint<CreatePersonalAccessTokenRequestDto, PersonalAccessTokenCreationDto>(
            uri = PatApiPath.TOKENS,
            dto = createPersonalAccessTokenRequest.copy(expiresIn = expiresIn),
            status = HttpStatusCode.OK,
            responseDtoCheck = { response ->
                assertThat(response).isNotNull()
                val creation = response!!
                assertThat(creation.id).isGreaterThan(0)
                assertThat(creation.name).isEqualTo(createPersonalAccessTokenRequest.name)
                assertThat(creation.token).matches("^[0-9a-f]{64}$")
                assertThat(creation.prefix).matches("^[0-9a-f]{8}$")
                assertThat(creation.createdAt).matches(DATE_TIME_PATTERN)
                assertThat(creation.expiresAt != null).isEqualTo(shouldExpire)
                creation.expiresAt?.let { assertThat(it).matches(DATE_TIME_PATTERN) }
            },
            authorize = ApplicationTestBuilder::registerAndLogin
        )
    }

    @After
    fun tearDown() {
        GlobalContext.stopKoin()
    }

    private companion object {
        val createPersonalAccessTokenRequest = CreatePersonalAccessTokenRequestDto(
            name = "CLI",
            expiresIn = "NEVER"
        )
        const val DATE_TIME_PATTERN = "^[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}$"
    }
}
