package com.marzec

import com.marzec.exercises.categoryOneDto
import com.marzec.exercises.categoryTwoDto
import com.marzec.exercises.equipmentOneDto
import com.marzec.exercises.equipmentTwoDto
import com.marzec.exercises.stubCreateTrainingTemplateDto
import com.marzec.exercises.stubCreateTrainingTemplatePartDto
import com.marzec.exercises.stubTrainingTemplateDto
import com.marzec.exercises.stubTrainingTemplatePartDto
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationEngine
import org.junit.After
import org.junit.Test
import org.koin.core.context.GlobalContext

class TrainingTemplateTests {

    private val createTrainingTemplateDto = stubCreateTrainingTemplateDto(
        name = "trainingTemplate",
        exercises = listOf(
            stubCreateTrainingTemplatePartDto(
                name = "part_one",
                pinnedExerciseId = null,
                categoryIds = listOf("1"),
                excludedExercisesIds = listOf(2),
                excludedEquipmentIds = listOf(),
            ),
            stubCreateTrainingTemplatePartDto(
                name = "part_two",
                pinnedExerciseId = null,
                categoryIds = listOf("2"),
                excludedExercisesIds = listOf(),
                excludedEquipmentIds = listOf("5")
            )
        ),
        availableEquipmentIds = listOf("4", "5")
    )

    private val trainingTemplateDto = stubTrainingTemplateDto(
        id = 1,
        name = "trainingTemplate",
        exercises = listOf(
            stubTrainingTemplatePartDto(
                id = 1,
                name = "part_one",
                pinnedExercise = null,
                categories = listOf(categoryOneDto),
                excludedExercises = listOf(2),
                excludedEquipment = listOf()
            ),
            stubTrainingTemplatePartDto(
                id = 2,
                name = "part_two",
                pinnedExercise = null,
                categories = listOf(categoryTwoDto),
                excludedExercises = listOf(),
                excludedEquipment = listOf(equipmentTwoDto)
            )
        ),
        availableEquipment = listOf(equipmentOneDto, equipmentTwoDto),
    )

    @Test
    fun putTemplate() {
        testPostEndpoint(
            uri = ApiPath.TRAINING_TEMPLATE,
            dto = createTrainingTemplateDto,
            status = HttpStatusCode.OK,
            responseDto = trainingTemplateDto,
            authorize = TestApplicationEngine::registerAndLogin
        )
    }

    @After
    fun tearDown() {
        GlobalContext.stopKoin()
    }
}