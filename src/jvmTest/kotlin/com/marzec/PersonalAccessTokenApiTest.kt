package com.marzec

import com.google.common.truth.Truth.assertThat
import com.marzec.fiteo.PatApiPath
import com.marzec.fiteo.model.dto.CreatePersonalAccessTokenRequestDto
import com.marzec.fiteo.model.dto.ErrorDto
import com.marzec.fiteo.model.dto.PersonalAccessTokenCreationDto
import io.ktor.client.request.*
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.*
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
