package com.marzec

import com.marzec.fiteo.ApiPath
import com.marzec.fiteo.model.dto.ErrorDto
import com.marzec.fiteo.model.dto.UserDto
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationEngine
import org.junit.After
import org.junit.Test
import org.koin.core.context.GlobalContext

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
                register()
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
                register()
            }
        )
    }

    @Test
    fun getUser() {
        testGetEndpoint(
            uri = ApiPath.USER,
            status = HttpStatusCode.OK,
            responseDto = UserDto(2, "test@mail.com"),
            authorize = TestApplicationEngine::registerAndLogin
        )
    }

    @Test
    fun logout() {
        testGetEndpoint(
            uri = ApiPath.LOGOUT,
            status = HttpStatusCode.OK,
            responseDto = Unit,
            authorize = TestApplicationEngine::registerAndLogin
        )
    }

    @After
    fun tearDown() {
        GlobalContext.stopKoin()
    }
}