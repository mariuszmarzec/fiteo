package com.marzec

import com.marzec.fiteo.ApiPath
import com.marzec.fiteo.model.domain.toDto
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

