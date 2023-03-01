package com.marzec

import com.marzec.fiteo.ApiPath
import com.marzec.fiteo.model.dto.CreateExerciseDto
import io.ktor.http.*
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
}