package com.marzec

import com.marzec.fiteo.ApiPath
import com.marzec.fiteo.model.dto.ErrorDto
import com.marzec.fiteo.model.dto.UserDto
import io.ktor.http.HttpStatusCode
import org.junit.After
import org.junit.Test
import org.koin.core.context.GlobalContext
import com.google.common.truth.Truth.assertThat
import com.marzec.core.CurrentTimeUtil
import com.marzec.di.NAME_SESSION_EXPIRATION_TIME
import io.ktor.server.testing.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.koin.core.qualifier.named

class AuthorizationTest {

    @Test
    fun register() {
        testPostEndpoint(
            ApiPath.REGISTRATION,
            registerRequestDto,
            HttpStatusCode.OK,
            UserDto(2, "test@mail.com")
        )
    }

    @Test
    fun register_blankEmail() {
        testPostEndpoint(
            ApiPath.REGISTRATION,
            registerRequestDto.copy(email = "   "),
            HttpStatusCode.BadRequest,
            ErrorDto("Empty email")
        )
    }

    @Test
    fun register_emptyEmail() {
        testPostEndpoint(
            ApiPath.REGISTRATION,
            registerRequestDto.copy(email = ""),
            HttpStatusCode.BadRequest,
            ErrorDto("Empty email")
        )
    }

    @Test
    fun register_toShortPassword() {
        testPostEndpoint(
            ApiPath.REGISTRATION,
            registerRequestDto.copy(password = "12345"),
            HttpStatusCode.BadRequest,
            ErrorDto("Password too short, min is 6")
        )
    }

    @Test
    fun register_differentPasswords() {
        testPostEndpoint(
            ApiPath.REGISTRATION,
            registerRequestDto.copy(password = "12345678901"),
            HttpStatusCode.BadRequest,
            ErrorDto("Passwords are different")
        )
    }

    @Test
    fun login() {
        testPostEndpoint(
            uri = ApiPath.LOGIN,
            dto = loginDto,
            status = HttpStatusCode.OK,
            responseDto = UserDto(2, "test@mail.com"),
            runRequestsBefore = {
                register(registerRequestDto)
            }
        )
    }

    @Test
    fun login_incorrectPassword() {
        testPostEndpoint(
            uri = ApiPath.LOGIN,
            dto = loginDto.copy(password = "1234567"),
            status = HttpStatusCode.BadRequest,
            responseDto = ErrorDto("Wrong password"),
            runRequestsBefore = {
                register(registerRequestDto)
            }
        )
    }

    @Test
    fun getUser() {
        testGetEndpoint(
            uri = ApiPath.USER,
            status = HttpStatusCode.OK,
            responseDto = UserDto(2, "test@mail.com"),
            authorize = ApplicationTestBuilder::registerAndLogin
        )
    }

    @Test
    fun logout() {
        testGetEndpoint(
            uri = ApiPath.LOGOUT,
            status = HttpStatusCode.OK,
            responseDto = Unit,
            authorize = ApplicationTestBuilder::registerAndLogin
        )
    }

    @Test
    fun clearSessions() {
        val mockSession = 1000L
        var token: String? = null
        CurrentTimeUtil.setOtherTime(16, 5, 2021)

        testGetEndpoint(
            uri = ApiPath.USER,
            status = HttpStatusCode.OK,
            responseDto = UserDto(2, "test@mail.com"),
            authorize = ApplicationTestBuilder::registerAndLogin,
            runRequestsAfter = {
                token = authToken
            }
        )

        CurrentTimeUtil.setOtherTime(17, 5, 2021)

        withMockTestApplication(
            withDbClear = false,
            mockConfiguration = {
                defaultMockConfiguration()
                single(qualifier = named(NAME_SESSION_EXPIRATION_TIME)) { mockSession }
            }
        ) {
            // Without this delay, db transaction removing entity from cached session table doesn't always finish
            // closing application. TODO find better solution in future
            runBlocking { delay(1000) }
        }

        withMockTestApplication(
            withDbClear = false,
            mockConfiguration = {
                defaultMockConfiguration()
                single(qualifier = named(NAME_SESSION_EXPIRATION_TIME)) { mockSession }
            }
        ) {
            authToken = token
            assertThat(getUserCall()).isNull()
        }
    }

    @After
    fun tearDown() {
        GlobalContext.stopKoin()
    }
}
