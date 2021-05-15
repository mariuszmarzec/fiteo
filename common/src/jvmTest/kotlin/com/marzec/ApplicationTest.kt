package com.marzec

import com.marzec.cheatday.ApiPath as CheatApiPath
import com.google.common.truth.Truth.assertThat
import com.marzec.exercises.categories
import com.marzec.exercises.createWeightDto
import com.marzec.exercises.equipment
import com.marzec.exercises.exercises
import com.marzec.exercises.loginDto
import com.marzec.exercises.registerRequestDto
import com.marzec.exercises.weightDto
import com.marzec.exercises.weightDto2
import com.marzec.exercises.weightDto3
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

class CheatDay {

    @Test
    fun putWeight() {
        testPostEndpoint(
            uri = CheatApiPath.WEIGHT,
            dto = createWeightDto,
            status = HttpStatusCode.OK,
            responseDto = weightDto,
            authorize = TestApplicationEngine::registerAndLogin
        )
    }

    @Test
    fun weights() {
        testGetEndpoint(
            uri = CheatApiPath.WEIGHTS,
            status = HttpStatusCode.OK,
            responseDto = listOf(
                weightDto,
                weightDto2,
                weightDto3
            ),
            authorize = TestApplicationEngine::registerAndLogin,
            runRequestsBefore = {
                addWeight(weightDto)
                addWeight(weightDto2)
                addWeight(weightDto3)
            }
        )
    }

    @Test
    fun removeWeight() {
        testDeleteEndpoint(
            uri = CheatApiPath.REMOVE_WEIGHT.replace("{id}", "2"),
            status = HttpStatusCode.OK,
            responseDto = weightDto2,
            authorize = TestApplicationEngine::registerAndLogin,
            runRequestsBefore = {
                addWeight(weightDto)
                addWeight(weightDto2)
                addWeight(weightDto3)
            },
            runRequestsAfter = {
                assertThat(getWeights()).isEqualTo(
                    listOf(
                        weightDto,
                        weightDto3
                    )
                )
            }
        )
    }

    @Test
    fun updateWeight() {
        testPatchEndpoint(
            uri = CheatApiPath.UPDATE_WEIGHT,
            dto = weightDto2.copy(value = 63.2f, date = "2021-05-18T07:20:30"),
            status = HttpStatusCode.OK,
            responseDto = weightDto2,
            authorize = TestApplicationEngine::registerAndLogin,
            runRequestsBefore = {
                addWeight(weightDto)
                addWeight(weightDto2)
                addWeight(weightDto3)
            },
            runRequestsAfter = {
                assertThat(getWeights()).isEqualTo(
                    listOf(
                        weightDto,
                        weightDto2.copy(value = 63.2f, date = "2021-05-18T07:20:30"),
                        weightDto3
                    )
                )
            }
        )
    }

    @After
    fun tearDown() {
        GlobalContext.stopKoin()
    }
}