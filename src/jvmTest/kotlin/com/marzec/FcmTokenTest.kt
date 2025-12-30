package com.marzec

import com.marzec.core.CurrentTimeUtil
import com.marzec.fiteo.ApiPath
import com.marzec.fiteo.model.dto.CreateFcmTokenDto
import com.marzec.fiteo.model.dto.FcmTokenDto
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.After
import org.junit.Test
import org.koin.core.context.GlobalContext

class FcmTokenTest {

    private val createFcmTokenDto = CreateFcmTokenDto(
        fcmToken = "test_token_123",
        platform = "android"
    )

    private val fcmTokenDto = FcmTokenDto(
        id = 1,
        userId = 2,
        fcmToken = "test_token_123",
        platform = "android",
        updatedAt = "2021-05-16T00:00:00"
    )

    @Test
    fun addFcmToken() {
        testPostEndpoint(
            uri = ApiPath.FCM_TOKEN,
            dto = createFcmTokenDto,
            status = HttpStatusCode.OK,
            responseDto = fcmTokenDto,
            authorize = ApplicationTestBuilder::registerAndLogin,
            runRequestsBefore = {
                CurrentTimeUtil.setOtherTime(16, 5, 2021)
            }
        )
    }

    @Test
    fun deleteFcmToken() {
        testDeleteEndpoint(
            uri = ApiPath.FCM_TOKEN + "/test_token_123",
            status = HttpStatusCode.OK,
            responseDto = Unit,
            authorize = ApplicationTestBuilder::registerAndLogin,
            runRequestsBefore = {
                CurrentTimeUtil.setOtherTime(16, 5, 2021)
                addFcmToken(createFcmTokenDto)
            }
        )
    }

    @After
    fun tearDown() {
        GlobalContext.stopKoin()
    }
}
