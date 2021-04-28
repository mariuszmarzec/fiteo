package com.marzec

import com.marzec.data.InitialDataLoader
import com.marzec.di.MainModule
import com.marzec.io.ExercisesReader
import com.marzec.io.ResourceFileReader
import com.marzec.model.dto.ExercisesFileDto
import org.junit.After
import org.junit.Before
import org.junit.Test
import io.ktor.server.testing.*
import io.ktor.http.*
import io.mockk.every
import io.mockk.mockk
import junit.framework.Assert.assertEquals
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest

class ApplicationTest {

    @Before
    fun setUp() {
        setupDb()
    }

    @Test
    fun testContainer() {
        withTestApplication({
            module(MainModule.plus(module {
                factory<ResourceFileReader>(override = true) { val mockk = mockk<ResourceFileReader>()
                    every { mockk.read(any()) } returns ""
                    mockk
                }
                factory<ExercisesReader>(override = true) { val mockk = mockk<ExercisesReader>()
                    every { mockk.parse(any()) } returns ExercisesFileDto(
                        null, null, null
                    )
                    mockk
                }
            })
            )
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

