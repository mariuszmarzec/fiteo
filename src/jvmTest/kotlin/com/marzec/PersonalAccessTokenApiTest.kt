package com.marzec

import com.google.common.truth.Truth.assertThat
import com.marzec.Api.Headers
import com.marzec.core.sha256Hex
import com.marzec.database.DbSettings
import com.marzec.exceptions.HttpException
import com.marzec.exceptions.HttpStatus
import com.marzec.fiteo.PatApiPath
import com.marzec.fiteo.model.dto.CreatePersonalAccessTokenRequestDto
import com.marzec.fiteo.model.dto.ErrorDto
import com.marzec.fiteo.model.dto.LoginRequestDto
import com.marzec.fiteo.model.dto.PersonalAccessTokenCreationDto
import com.marzec.fiteo.model.dto.PersonalAccessTokenDto
import com.marzec.fiteo.model.dto.RegisterRequestDto
import com.marzec.fiteo.services.PersonalAccessTokenService
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.*
import org.junit.After
import org.junit.Test
import org.koin.core.context.GlobalContext
import org.koin.core.parameter.parametersOf
import kotlin.test.assertFailsWith

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
    fun listPersonalAccessTokens_returnsOnlyAuthenticatedUsersTokens() {
        lateinit var userAuth: String
        lateinit var firstToken: PersonalAccessTokenCreationDto
        lateinit var secondToken: PersonalAccessTokenCreationDto
        lateinit var otherUsersToken: PersonalAccessTokenCreationDto

        testGetEndpointCheck<List<PersonalAccessTokenDto>>(
            uri = PatApiPath.TOKENS,
            status = HttpStatusCode.OK,
            responseDtoCheck = { response ->
                assertThat(response?.map { Triple(it.id, it.name, it.tokenPrefix) }).containsExactly(
                    Triple(firstToken.id, firstToken.name, firstToken.prefix),
                    Triple(secondToken.id, secondToken.name, secondToken.prefix)
                )
            },
            authorize = {
                userAuth = registerAndLogin()
                userAuth
            },
            runRequestsBefore = {
                firstToken = createToken(name = "CLI", expiresIn = "NEVER")
                secondToken = createToken(name = "Mobile", expiresIn = "THREE_MONTHS")

                register(
                    RegisterRequestDto(
                        email = "other-user@mail.com",
                        password = "1234567890",
                        repeatedPassword = "1234567890"
                    )
                )
                authToken = login(LoginRequestDto(email = "other-user@mail.com", password = "1234567890"))
                otherUsersToken = createToken(name = "Other user's token", expiresIn = "NEVER")
                authToken = userAuth
            },
            runRequestsAfter = {
                val responseBody = client.get(PatApiPath.TOKENS) {
                    header(Headers.AUTHORIZATION, userAuth)
                }.bodyAsText()

                assertThat(
                    listOf(firstToken.token, secondToken.token, otherUsersToken.token)
                        .filter { responseBody.contains(it) }
                ).isEmpty()
                assertThat(
                    listOf(firstToken.token, secondToken.token, otherUsersToken.token)
                        .map { sha256Hex(it.encodeToByteArray()) }
                        .filter { responseBody.contains(it) }
                ).isEmpty()
                assertThat(responseBody).doesNotContain("tokenHash")
                assertThat(responseBody).doesNotContain("\"token\":")
            }
        )
    }

    @Test
    fun personalAccessTokenLifecycle_createListVerifyAndRevokeToken() {
        withDefaultMockTestApplication {
            authToken = registerAndLogin()
            val token = createToken(name = "Lifecycle test", expiresIn = "NEVER")
            val tokenService = GlobalContext.get().get<PersonalAccessTokenService> {
                parametersOf(DbSettings.database)
            }

            assertThat(tokenService.verify(token.token).id).isGreaterThan(0)

            val listResponse = client.get(PatApiPath.TOKENS) {
                header(Headers.AUTHORIZATION, authToken!!)
            }
            assertThat(listResponse.status).isEqualTo(HttpStatusCode.OK)
            val listBody = listResponse.bodyAsText()
            assertThat(listBody).contains(token.id.toString())
            assertThat(listBody).contains("Lifecycle test")
            assertThat(listBody).doesNotContain(token.token)
            assertThat(listBody).doesNotContain(sha256Hex(token.token.encodeToByteArray()))
            assertThat(listBody).doesNotContain("tokenHash")

            val deleteResponse = client.delete(tokenPath(token.id)) {
                header(Headers.AUTHORIZATION, authToken!!)
            }
            assertThat(deleteResponse.status).isEqualTo(HttpStatusCode.OK)

            assertThat(getWithAuth<List<PersonalAccessTokenDto>>(PatApiPath.TOKENS).map { it.id })
                .doesNotContain(token.id)
            val verificationFailure = assertFailsWith<HttpException> {
                tokenService.verify(token.token)
            }
            assertThat(verificationFailure.httpStatus).isEqualTo(HttpStatus.UNAUTHORIZED)
        }
    }

    @Test
    fun deletePersonalAccessToken_requiresAuthentication() {
        withDefaultMockTestApplication {
            val response = client.delete(tokenPath(1))

            assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
        }
    }

    @Test
    fun deletePersonalAccessToken_deletesOwnToken() {
        withDefaultMockTestApplication {
            authToken = registerAndLogin()
            val token = createToken(name = "CLI", expiresIn = "NEVER")

            val response = client.delete(tokenPath(token.id)) {
                header(Headers.AUTHORIZATION, authToken!!)
            }

            assertThat(response.status).isEqualTo(HttpStatusCode.OK)
            assertThat(getWithAuth<List<PersonalAccessTokenDto>>(PatApiPath.TOKENS).map { it.id })
                .doesNotContain(token.id)
        }
    }

    @Test
    fun deletePersonalAccessToken_doesNotDeleteAnotherUsersToken() {
        withDefaultMockTestApplication {
            val userAuth = registerAndLogin()
            authToken = userAuth
            val userToken = createToken(name = "Mine", expiresIn = "NEVER")

            val otherUserCredentials = RegisterRequestDto(
                email = "other-user@mail.com",
                password = "1234567890",
                repeatedPassword = "1234567890"
            )
            register(otherUserCredentials)
            val otherUserAuth = login(
                LoginRequestDto(email = otherUserCredentials.email, password = otherUserCredentials.password)
            )
            authToken = otherUserAuth
            val otherUsersToken = createToken(name = "Other user's token", expiresIn = "NEVER")

            val response = client.delete(tokenPath(otherUsersToken.id)) {
                header(Headers.AUTHORIZATION, userAuth)
            }

            assertThat(response.status).isEqualTo(HttpStatusCode.OK)
            authToken = userAuth
            assertThat(getWithAuth<List<PersonalAccessTokenDto>>(PatApiPath.TOKENS).map { it.id })
                .containsExactly(userToken.id)

            authToken = otherUserAuth
            assertThat(getWithAuth<List<PersonalAccessTokenDto>>(PatApiPath.TOKENS).map { it.id })
                .containsExactly(otherUsersToken.id)
        }
    }

    private fun tokenPath(id: Int) = PatApiPath.TOKEN_BY_ID.replace("{id}", id.toString())

    private suspend fun ApplicationTestBuilder.createToken(
        name: String,
        expiresIn: String
    ): PersonalAccessTokenCreationDto = postWithAuth(
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
