package com.marzec

import com.marzec.fiteo.ApiPath
import com.marzec.fiteo.model.dto.ErrorDto
import com.marzec.fiteo.model.dto.UserDto
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationEngine
import org.junit.After
import org.junit.Test
import org.koin.core.context.GlobalContext
import com.google.common.truth.Truth.assertThat
import com.marzec.core.CurrentTimeUtil
import com.marzec.di.NAME_SESSION_EXPIRATION_TIME
import io.ktor.server.application.Application
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.koin.core.qualifier.named

class BearerAuthorizationTest {

    val user = UserDto(1, "mariusz.marzec00@gmail.com")
    @Test
    fun test() {
        testGetEndpoint(
                ApiPath.USERS,
                HttpStatusCode.OK,
                listOf(user)
        )
    }
}