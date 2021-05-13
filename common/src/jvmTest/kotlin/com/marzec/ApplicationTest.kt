package com.marzec

import com.marzec.exercises.categories
import com.marzec.exercises.equipment
import com.marzec.exercises.exercises
import com.marzec.exercises.loginDto
import com.marzec.exercises.registerRequestDto
import com.marzec.model.domain.toDto
import com.marzec.model.dto.ErrorDto
import com.marzec.model.dto.UserDto
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationEngine
import org.junit.After
import org.junit.Test
import org.koin.core.context.GlobalContext

class FiteoCoreTest {

    @Test
    fun exercises() {
        testGetEndpoint(
            ApiPath.EXERCISES,
            HttpStatusCode.OK,
            exercises.map { it.toDto() }
        )
    }

    @Test
    fun equipment() {
        testGetEndpoint(
            ApiPath.EQUIPMENT,
            HttpStatusCode.OK,
            equipment.map { it.toDto() }
        )
    }

    @Test
    fun categories() {
        testGetEndpoint(
            ApiPath.CATEGORIES,
            HttpStatusCode.OK,
            categories.map { it.toDto() }
        )
    }

    @After
    fun tearDown() {
        GlobalContext.stopKoin()
    }
}

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
            ApiPath.LOGIN,
            loginDto,
            HttpStatusCode.OK,
            UserDto(2, "test@mail.com")
        ) {
            register()
        }
    }

    @Test
    fun login_incorrectPassword() {
        testPostEndpoint(
            ApiPath.LOGIN,
            loginDto.copy(password = "1234567"),
            HttpStatusCode.BadRequest,
            ErrorDto("Wrong password")
        ) {
            register()
        }
    }

    @Test
    fun logout() {
        testGetEndpoint(
            ApiPath.LOGOUT,
            HttpStatusCode.OK,
            Unit,
            TestApplicationEngine::registerAndLogin
        )
    }

    @After
    fun tearDown() {
        GlobalContext.stopKoin()
    }
}

