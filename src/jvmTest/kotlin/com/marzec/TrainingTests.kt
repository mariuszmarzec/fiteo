package com.marzec

import com.google.common.truth.Truth.assertThat
import com.marzec.core.CurrentTimeUtil
import com.marzec.fiteo.ApiPath
import com.marzec.fiteo.model.domain.TrainingDto
import com.marzec.fiteo.model.domain.toDto
import com.marzec.fiteo.model.dto.ErrorDto
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

    private val createTrainingTemplateDto2 = stubCreateTrainingTemplateDto(
        name = "trainingTemplate",
        exercises = listOf(
            stubCreateTrainingTemplatePartDto(
                name = "part_one",
                pinnedExerciseId = null,
                categoryIds = listOf(),
                excludedExercisesIds = listOf(2),
                excludedEquipmentIds = listOf(),
            )
        ),
        availableEquipmentIds = listOf("4", "5")
    )

    private val trainingDto = stubTraining(
        id = 1,
        templateId = 1,
        exercisesWithProgress = listOf(
            stubTrainingExerciseWithProgressDto(
                exercise = exerciseCategoryOneEquipment0ne.toDto(),
                templatePartId = 1,
                name = "part_one"
            ),
            stubTrainingExerciseWithProgressDto(
                exercise = exerciseCategoryTwoEquipmentOne.toDto(),
                templatePartId = 2,
                name = "part_two"
            )
        )
    )

    private val updateDto = stubUpdateTrainingDto(
        exercisesWithProgress = listOf(
            stubUpdateTrainingExerciseWithProgressDto(
                exerciseId = 1,
                series = listOf(
                    stubSeriesDto(
                        exerciseId = 1,
                        trainingId = 1,
                        burden = 10.4f
                    )
                ),
                trainingPartId = 1,
                name = "part_one",
            ),
            stubUpdateTrainingExerciseWithProgressDto(
                exerciseId = 4,
                series = listOf(
                    stubSeriesDto(
                        exerciseId = 4,
                        trainingId = 1,
                        burden = 3f,
                        repsNumber = 10,
                        note = "note"
                    )
                ),
                trainingPartId = 2,
                name = "part_updated"
            )
        )
    )

    private val updatedTraining = stubTraining(
        id = 1,
        templateId = 1,
        exercisesWithProgress = listOf(
            stubTrainingExerciseWithProgressDto(
                templatePartId = 1,
                name = "part_one",
                exercise = exerciseCategoryOneEquipment0ne.toDto(),
                series = listOf(
                    stubSeriesDto(
                        seriesId = 1,
                        exerciseId = 1,
                        trainingId = 1,
                        burden = 10.4f
                    )
                )
            ),
            stubTrainingExerciseWithProgressDto(
                templatePartId = 2,
                name = "part_updated",
                exercise = exerciseCategoryTwoEquipmentOne.toDto(),
                series = listOf(
                    stubSeriesDto(
                        seriesId = 2,
                        exerciseId = 4,
                        trainingId = 1,
                        burden = 3f,
                        repsNumber = 10,
                        note = "note"
                    )
                )
            )
        )
    )

    @Test
    fun createTraining() {
        testGetEndpoint(
            uri = ApiPath.CREATE_TRAINING.replace("{${Api.Args.ARG_ID}}", "1"),
            status = HttpStatusCode.OK,
            responseDto = trainingDto,
            authorize = TestApplicationEngine::registerAndLogin,
            runRequestsBefore = {
                CurrentTimeUtil.setOtherTime(16, 5, 2021)
                putTemplate(createTrainingTemplateDto)
            }
        )
    }

    @Test
    fun createTraining_doNotCreateTrainingInCaseOfError() {
        testGetEndpoint<ErrorDto>(
            uri = ApiPath.CREATE_TRAINING.replace("{${Api.Args.ARG_ID}}", "1"),
            status = HttpStatusCode.InternalServerError,
            responseDtoCheck = {
                assertThat(it).isInstanceOf(ErrorDto::class.java)
                assertThat((it as ErrorDto).reason).contains("No exercise for training part with: 1")
            },
            authorize = TestApplicationEngine::registerAndLogin,
            runRequestsBefore = {
                CurrentTimeUtil.setOtherTime(16, 5, 2021)
                putTemplate(createTrainingTemplateDto2)
            }, runRequestsAfter = {
                assertThat(getTrainings()).isEqualTo(emptyList<TrainingDto>())
            }
        )
    }

    @Test
    fun getTraining() {
        testGetEndpoint(
            uri = ApiPath.TRAINING.replace("{${Api.Args.ARG_ID}}", "1"),
            status = HttpStatusCode.OK,
            responseDto = trainingDto,
            authorize = TestApplicationEngine::registerAndLogin,
            runRequestsBefore = {
                CurrentTimeUtil.setOtherTime(16, 5, 2021)
                putTemplate(createTrainingTemplateDto)
                createTraining("1")
            }
        )
    }

    @Test
    fun getTrainings() {
        testGetEndpoint(
            uri = ApiPath.TRAININGS,
            status = HttpStatusCode.OK,
            responseDto = listOf(
                trainingDto.copy(
                    id = 2,
                    createDateInMillis = dateTime4,
                    finishDateInMillis = dateTime4
                ),
                trainingDto
            ),
            authorize = TestApplicationEngine::registerAndLogin,
            runRequestsBefore = {
                CurrentTimeUtil.setOtherTime(16, 5, 2021)
                putTemplate(createTrainingTemplateDto)
                createTraining("1")
                CurrentTimeUtil.setOtherTime(19, 5, 2021)
                createTraining("1")
            }
        )
    }

    @Test
    fun deleteTraining() {
        testDeleteEndpoint(
            uri = ApiPath.TRAINING.replace("{${Api.Args.ARG_ID}}", "1"),
            status = HttpStatusCode.OK,
            responseDto = trainingDto,
            authorize = TestApplicationEngine::registerAndLogin,
            runRequestsBefore = {
                CurrentTimeUtil.setOtherTime(16, 5, 2021)
                putTemplate(createTrainingTemplateDto)
                createTraining("1")
            },
            runRequestsAfter = {
                assertThat(getTrainings()).isEqualTo(emptyList<TrainingDto>())
            }
        )
    }

    @Test
    fun updateTraining() {
        testPatchEndpoint(
            uri = ApiPath.TRAINING.replace("{${Api.Args.ARG_ID}}", "1"),
            dto = updateDto,
            status = HttpStatusCode.OK,
            responseDto = updatedTraining,
            authorize = TestApplicationEngine::registerAndLogin,
            runRequestsBefore = {
                CurrentTimeUtil.setOtherTime(16, 5, 2021)
                putTemplate(createTrainingTemplateDto)
                createTraining("1")
            },
            runRequestsAfter = {
                assertThat(getTrainings()).isEqualTo(listOf(updatedTraining))
            }
        )
    }


    @After
    fun tearDown() {
        GlobalContext.stopKoin()
    }
}
