package com.marzec

import com.google.common.truth.Truth.assertThat
import com.marzec.fiteo.ApiPath
import com.marzec.fiteo.model.domain.toDto
import com.marzec.fiteo.model.dto.CreateExerciseDto
import io.ktor.http.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.encodeToJsonElement
import org.junit.Test

class ExerciseTests {

    val createdExerciseId = 8
    val exerciseName = "exercise_name"
    val animationUrl = "animationUrl"
    val videoUrl = "videoUrl"
    val thumbnailUrl = "thumbnailUrl"

    val createExerciseDto = CreateExerciseDto(
        name = exerciseName,
        animationUrl = animationUrl,
        videoUrl = videoUrl,
        category = listOf(categoryOneDto),
        neededEquipment = listOf(equipmentOneDto, equipmentTwoDto),
        thumbnailUrl = thumbnailUrl
    )

    val exerciseDto = stubExerciseDto(
        id = createdExerciseId,
        name = exerciseName,
        animationUrl = animationUrl,
        videoUrl = videoUrl,
        category = listOf(categoryOneDto),
        neededEquipment = listOf(equipmentOneDto, equipmentTwoDto),
        thumbnailUrl = thumbnailUrl
    )

    @Test
    fun addExercise() {
        testPostEndpoint(
            uri = ApiPath.EXERCISES,
            dto = createExerciseDto,
            status = HttpStatusCode.OK,
            responseDto = exerciseDto
        )
    }

    @Test
    fun patchExercise() {
        testPatchEndpoint(
            uri = ApiPath.EXERCISE.replace("{${Api.Args.ARG_ID}}", "8"),
            dto = mapOf(
                "name" to JsonPrimitive("updatedName"),
                "animationUrl" to JsonObject(mapOf("value" to JsonPrimitive("updatedAnimationUrl"))),
                "category" to Json.encodeToJsonElement(listOf(categoryTwoDto)),
                "neededEquipment" to JsonPrimitive(null as String?),
                "videoUrl" to JsonPrimitive(null as String?),
            ),
            status = HttpStatusCode.OK,
            responseDto = exerciseDto.copy(
                name = "updatedName",
                animationUrl = "updatedAnimationUrl",
                category = listOf(categoryTwoDto)
            ),
            runRequestsBefore = {
                addExercise(createExerciseDto)
            }
        )
    }

    @Test
    fun getExercise() {
        testGetEndpoint(
            uri = ApiPath.EXERCISE.replace("{${Api.Args.ARG_ID}}", "8"),
            status = HttpStatusCode.OK,
            responseDto = exerciseDto,
            runRequestsBefore = {
                addExercise(createExerciseDto)
            }
        )
    }

    @Test
    fun deleteExercise() {
        testDeleteEndpoint(
            uri = ApiPath.EXERCISE.replace("{${Api.Args.ARG_ID}}", "8"),
            status = HttpStatusCode.OK,
            responseDto = exerciseDto,
            runRequestsBefore = {
                addExercise(createExerciseDto)
            },
            runRequestsAfter = {
                val allExercises = exercises.map { it.toDto() }

                assertThat(getExercises()).isEqualTo(allExercises)
            }
        )
    }
}