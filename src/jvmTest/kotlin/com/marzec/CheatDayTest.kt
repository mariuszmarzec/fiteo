package com.marzec

import com.google.common.truth.Truth
import com.marzec.cheatday.ApiPath
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.*
import org.junit.After
import org.junit.Test
import org.koin.core.context.GlobalContext

class CheatDayTest {

    @Test
    fun putWeight() {
        testPostEndpoint(
            uri = ApiPath.WEIGHT,
            dto = createWeightDto,
            status = HttpStatusCode.OK,
            responseDto = weightDto,
            authorize = ApplicationTestBuilder::registerAndLogin
        )
    }

    @Test
    fun weights() {
        testGetEndpoint(
            uri = ApiPath.WEIGHTS,
            status = HttpStatusCode.OK,
            responseDto = listOf(
                weightDto3,
                weightDto2,
                weightDto
            ),
            authorize = ApplicationTestBuilder::registerAndLogin,
            runRequestsBefore = {
                addWeight(weightDto)
                addWeight(weightDto2)
                addWeight(weightDto3)
            }
        )
    }

    @Test
    fun weightsBrokenDateFormat() {
        testGetEndpoint(
            uri = ApiPath.WEIGHTS,
            status = HttpStatusCode.OK,
            responseDto = listOf(
                weightDto.copy(
                    date = "2020-01-02T00:00:00"
                )

            ),
            authorize = ApplicationTestBuilder::registerAndLogin,
            runRequestsBefore = {
                addWeight(
                    weightDto.copy(
                        date = "2020-01-02T00:00"
                    )
                )
            }
        )
    }

    @Test
    fun removeWeight() {
        testDeleteEndpoint(
            uri = ApiPath.REMOVE_WEIGHT.replace("{id}", "2"),
            status = HttpStatusCode.OK,
            responseDto = weightDto2,
            authorize = ApplicationTestBuilder::registerAndLogin,
            runRequestsBefore = {
                addWeight(weightDto)
                addWeight(weightDto2)
                addWeight(weightDto3)
            },
            runRequestsAfter = {
                Truth.assertThat(getWeights()).isEqualTo(
                    listOf(
                        weightDto3,
                        weightDto
                    )
                )
            }
        )
    }

    @Test
    fun updateWeight() {
        testPatchEndpoint(
            uri = ApiPath.UPDATE_WEIGHT,
            dto = weightDto2.copy(value = 63.2f, date = "2021-05-18T07:20:30"),
            status = HttpStatusCode.OK,
            responseDto = weightDto2.copy(value = 63.2f, date = "2021-05-18T07:20:30"),
            authorize = ApplicationTestBuilder::registerAndLogin,
            runRequestsBefore = {
                addWeight(weightDto)
                addWeight(weightDto2)
                addWeight(weightDto3)
            },
            runRequestsAfter = {
                Truth.assertThat(getWeights()).isEqualTo(
                    listOf(
                        weightDto2.copy(value = 63.2f, date = "2021-05-18T07:20:30"),
                        weightDto3,
                        weightDto,
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

