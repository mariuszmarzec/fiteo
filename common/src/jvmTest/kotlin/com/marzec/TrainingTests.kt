package com.marzec

import com.marzec.core.CurrentTimeUtil
import com.marzec.exercises.exerciseCategoryOneEquipment0ne
import com.marzec.exercises.exerciseCategoryTwoEquipmentOne
import com.marzec.exercises.stubCreateTrainingTemplateDto
import com.marzec.exercises.stubCreateTrainingTemplatePartDto
import com.marzec.exercises.stubTraining
import com.marzec.exercises.stubTrainingExerciseWithProgressDto
import com.marzec.model.domain.toDto
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationEngine
import org.junit.After
import org.junit.Test
import org.koin.core.context.GlobalContext

class TrainingTests {

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

    val responseDto = stubTraining(
        id = 1,
        templateId = 1,
        exercisesWithProgress = listOf(
            stubTrainingExerciseWithProgressDto(
                exercise = exerciseCategoryOneEquipment0ne.toDto()
            ),
            stubTrainingExerciseWithProgressDto(
                exercise = exerciseCategoryTwoEquipmentOne.toDto()
            )
        )
    )

    @Test
    fun createTraining() {
        testGetEndpoint(
            uri = ApiPath.CREATE_TRAINING.replace("{${ApiPath.ARG_ID}}", "1"),
            status = HttpStatusCode.OK,
            responseDto = responseDto,
            authorize = TestApplicationEngine::registerAndLogin,
            runRequestsBefore = {
                CurrentTimeUtil.setOtherTime(16, 5, 2021)
                putTemplate(createTrainingTemplateDto)
            }
        )
    }

    @After
    fun tearDown() {
        GlobalContext.stopKoin()
    }
}