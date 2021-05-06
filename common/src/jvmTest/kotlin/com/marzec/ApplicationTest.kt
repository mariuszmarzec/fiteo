package com.marzec

import com.google.common.truth.Truth.assertThat
import com.marzec.exercises.categories
import com.marzec.exercises.equipment
import com.marzec.exercises.exercises
import com.marzec.model.domain.toDto
import com.marzec.model.dto.CategoryDto
import com.marzec.model.dto.EquipmentDto
import com.marzec.model.dto.ExerciseDto
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import org.junit.After
import org.junit.Test
import org.koin.core.context.GlobalContext

class FiteoCoreTest {

    @Test
    fun exercises() {
        withDefaultMockTestApplication {
            handleRequest(HttpMethod.Get, ApiPath.EXERCISES).apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.OK)
                assertThatJson<List<ExerciseDto>>(response.content).isEqualTo(exercises.map { it.toDto() })
            }
        }
    }

    @Test
    fun equipment() {
        withDefaultMockTestApplication {
            handleRequest(HttpMethod.Get, ApiPath.EQUIPMENT).apply {
                assertThatJson<List<EquipmentDto>>(response.content).isEqualTo(equipment.map { it.toDto() })
                assertThat(response.status()).isEqualTo(HttpStatusCode.OK)
            }
        }
    }

    @Test
    fun categories() {
        withDefaultMockTestApplication {
            handleRequest(HttpMethod.Get, ApiPath.CATEGORIES).apply {
                assertThatJson<List<CategoryDto>>(response.content).isEqualTo(categories.map { it.toDto() })
                assertThat(response.status()).isEqualTo(HttpStatusCode.OK)
            }
        }
    }

    @After
    fun tearDown() {
        GlobalContext.stopKoin()
    }
}
