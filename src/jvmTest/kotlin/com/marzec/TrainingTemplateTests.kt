package com.marzec

import com.google.common.truth.Truth.assertThat
import com.marzec.fiteo.ApiPath
import com.marzec.fiteo.model.domain.TrainingTemplateDto
import com.marzec.fiteo.model.domain.toDto
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.*
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

    private val updateTrainingTemplateDto = stubCreateTrainingTemplateDto(
        id = 1,
        name = "trainingTemplateUpdated",
        exercises = listOf(
            stubCreateTrainingTemplatePartDto(
                name = "part_two",
                pinnedExerciseId = null,
                categoryIds = listOf("2"),
                excludedExercisesIds = listOf(),
                excludedEquipmentIds = listOf("5")
            ),
            stubCreateTrainingTemplatePartDto(
                name = "part_three",
                pinnedExerciseId = 3,
                categoryIds = listOf("1"),
                excludedExercisesIds = listOf(2),
                excludedEquipmentIds = listOf("6"),
            ),
        ),
        availableEquipmentIds = listOf("4", "5")
    )

    private val updatedTrainingTemplateDto = stubTrainingTemplateDto(
        id = 1,
        name = "trainingTemplateUpdated",
        exercises = listOf(
            stubTrainingTemplatePartDto(
                id = 3,
                name = "part_two",
                pinnedExercise = null,
                categories = listOf(categoryTwoDto),
                excludedExercises = listOf(),
                excludedEquipment = listOf(equipmentTwoDto)
            ),
            stubTrainingTemplatePartDto(
                id = 4,
                name = "part_three",
                pinnedExercise = exerciseCategoryOneEquipmentThree.toDto(),
                categories = listOf(categoryOneDto),
                excludedExercises = listOf(2),
                excludedEquipment = listOf(equipmentThreeDto)
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
            authorize = ApplicationTestBuilder::registerAndLogin
        )
    }

    @Test
    fun getTemplates() {
        testGetEndpoint(
            uri = ApiPath.TRAINING_TEMPLATES,
            status = HttpStatusCode.OK,
            responseDto = listOf(trainingTemplateDto),
            authorize = ApplicationTestBuilder::registerAndLogin,
            runRequestsBefore = {
                putTemplate(createTrainingTemplateDto)
            }
        )
    }

    @Test
    fun removeTemplate() {
        testDeleteEndpoint(
            uri = ApiPath.DELETE_TRAINING_TEMPLATES.replace("{id}", "1"),
            status = HttpStatusCode.OK,
            responseDto = trainingTemplateDto,
            authorize = ApplicationTestBuilder::registerAndLogin,
            runRequestsBefore = {
                putTemplate(createTrainingTemplateDto)
            },
            runRequestsAfter = {
                assertThat(getTemplates()).isEqualTo(
                    emptyList<TrainingTemplateDto>()
                )
            }
        )
    }

    @Test
    fun updateTemplate() {
        testPatchEndpoint(
            uri = ApiPath.UPDATE_TRAINING_TEMPLATES,
            dto = updateTrainingTemplateDto,
            status = HttpStatusCode.OK,
            responseDto = updatedTrainingTemplateDto,
            authorize = ApplicationTestBuilder::registerAndLogin,
            runRequestsBefore = {
                putTemplate(createTrainingTemplateDto)
            },
            runRequestsAfter = {
                assertThat(getTemplates()).isEqualTo(
                    listOf(updatedTrainingTemplateDto)
                )
            }
        )
    }

    @After
    fun tearDown() {
        GlobalContext.stopKoin()
    }
}
