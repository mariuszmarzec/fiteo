package com.marzec

import com.marzec.exercises.categories
import com.marzec.exercises.equipment
import com.marzec.exercises.exercises
import com.marzec.exercises.stubRegisterRequestDto
import com.marzec.model.domain.toDto
import com.marzec.model.dto.ErrorDto
import com.marzec.model.dto.UserDto
import io.ktor.http.HttpStatusCode
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

    private val registerRequestDto = stubRegisterRequestDto(
        email = "test@mail.com",
        password = "1234567890",
        repeatedPassword = "1234567890"
    )

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

//    @Test
//    fun login() {
//        withDefaultMockTestApplication {
//            handleRequest(HttpMethod.Post, ApiPath.LOGIN) {  }
//        }
//    }

    @After
    fun tearDown() {
        GlobalContext.stopKoin()
    }
}

