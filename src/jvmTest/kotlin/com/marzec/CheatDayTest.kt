package com.marzec

import com.google.common.truth.Truth.assertThat
import com.marzec.cheatday.ApiPath
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.After
import org.junit.Test
import org.koin.core.context.GlobalContext

class CheatDayTest {

    @Test
    @Deprecated("")
    fun putWeightDeprecated() {
        testPostEndpoint(
            uri = ApiPath.WEIGHT,
            dto = createWeightDto,
            status = HttpStatusCode.OK,
            responseDto = weightDto,
            authorize = ApplicationTestBuilder::registerAndLogin
        )
    }

    @Test
    fun putWeight() {
        testPostEndpoint(
            uri = ApiPath.WEIGHTS,
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
    fun getWeightById() {
        testGetEndpoint(
            uri = ApiPath.WEIGHT_BY_ID.replace("{id}", "2"),
            status = HttpStatusCode.OK,
            responseDto = weightDto,
            authorize = ApplicationTestBuilder::registerAndLogin,
            runRequestsBefore = {
                addWeight(weightDto)
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
    @Deprecated("")
    fun removeWeightDeprecated() {
        testDeleteEndpoint(
            uri = ApiPath.REMOVE_WEIGHT_DEPRECATED.replace("{id}", "2"),
            status = HttpStatusCode.OK,
            responseDto = weightDto2,
            authorize = ApplicationTestBuilder::registerAndLogin,
            runRequestsBefore = {
                addWeight(weightDto)
                addWeight(weightDto2)
                addWeight(weightDto3)
            },
            runRequestsAfter = {
                assertThat(getWeights()).isEqualTo(
                    listOf(
                        weightDto3,
                        weightDto
                    )
                )
            }
        )
    }

    @Test
    @Deprecated("")
    fun updateWeightDeprecated() {
        testPatchEndpoint(
            uri = ApiPath.UPDATE_WEIGHT_DEPRECATED,
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
                assertThat(getWeights()).isEqualTo(
                    listOf(
                        weightDto2.copy(value = 63.2f, date = "2021-05-18T07:20:30"),
                        weightDto3,
                        weightDto,
                    )
                )
            }
        )
    }

    @Test
    fun removeWeight() {
        testDeleteEndpoint(
            uri = ApiPath.WEIGHT_BY_ID.replace("{id}", "2"),
            status = HttpStatusCode.OK,
            responseDto = weightDto2,
            authorize = ApplicationTestBuilder::registerAndLogin,
            runRequestsBefore = {
                addWeight(weightDto)
                addWeight(weightDto2)
                addWeight(weightDto3)
            },
            runRequestsAfter = {
                assertThat(getWeights()).isEqualTo(
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
            uri = ApiPath.WEIGHT_BY_ID.replace("{id}", "2"),
            dto = """
                {
                    "value": 63.2f,
                    "date": "2021-05-18T07:20:30"
                }
            """.trimIndent(),
            status = HttpStatusCode.OK,
            responseDto = weightDto2.copy(value = 63.2f, date = "2021-05-18T07:20:30"),
            authorize = ApplicationTestBuilder::registerAndLogin,
            runRequestsBefore = {
                addWeight(weightDto)
                addWeight(weightDto2)
                addWeight(weightDto3)
            },
            runRequestsAfter = {
                assertThat(getWeights()).isEqualTo(
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

