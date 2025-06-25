package com.marzec

import com.marzec.fiteo.ApiPath
import com.marzec.fiteo.model.dto.LoginRequestDto
import com.marzec.fiteo.model.dto.UserDto
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.After
import org.junit.Test
import org.koin.core.context.GlobalContext

class BearerAuthorizationTest {

    val user = UserDto(1, "mariusz.marzec00@gmail.com")

    @Test
    fun login() {
        testPostEndpoint(
            uri = ApiPath.LOGIN_BEARER,
            dto = LoginRequestDto(email = "mariusz.marzec00@gmail.com", password = "password"),
            status = HttpStatusCode.OK,
            responseDto = user,
        )
    }

    @Test
    fun test() {
        testGetEndpoint(
            ApiPath.USERS,
            HttpStatusCode.OK,
            listOf(user),
            authorize = ApplicationTestBuilder::loginBearer
        )
    }

    @After
    fun tearDown() {
        GlobalContext.stopKoin()
    }
}