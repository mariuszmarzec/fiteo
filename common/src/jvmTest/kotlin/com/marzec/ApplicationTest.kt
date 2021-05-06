package com.marzec

import com.marzec.io.ExercisesReader
import com.marzec.io.ResourceFileReader
import com.marzec.model.dto.ExercisesFileDto
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.mockk.every
import junit.framework.Assert.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Test

class ApplicationTest {

    @Before
    fun setUp() {
        setupDb()
    }

    @Test
    fun testContainer() {
        withMockTestApplication(
            mockConfiguration = {
                factoryMock<ResourceFileReader> { mockk ->
                    every { mockk.read(any()) } returns ""
                }
                factoryMock<ExercisesReader> { mockk ->
                    every { mockk.parse(any()) } returns ExercisesFileDto(
                        null, null, null
                    )
                }
            }) {
            handleRequest(HttpMethod.Get, ApiPath.EQUIPMENT).apply {
                assertEquals("DZIALA1", response.content)
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }

    @After
    fun cleanUp() {
    }
}
