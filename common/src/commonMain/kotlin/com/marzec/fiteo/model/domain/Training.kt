package com.marzec.fiteo.model.domain

import com.marzec.extensions.formatDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

data class Training(
        val id: Int,
        val templateId: Int,
        val createDateInMillis: LocalDateTime,
        val finishDateInMillis: LocalDateTime,
        val exercisesWithProgress: List<TrainingExerciseWithProgress>
)

@Serializable
data class TrainingDto(
        val id: Int,
        val templateId: Int,
        val createDateInMillis: String,
        val finishDateInMillis: String,
        val exercisesWithProgress: List<TrainingExerciseWithProgressDto>
)

fun Training.toDto() = TrainingDto(
id = id,
templateId = templateId,
createDateInMillis = createDateInMillis.formatDate(),
finishDateInMillis = finishDateInMillis.formatDate(),
exercisesWithProgress = exercisesWithProgress.map { it.toDto() }
)

data class CreateTraining(
        val finishDateInMillis: LocalDateTime,
        val exercisesWithProgress: List<CreateTrainingExerciseWithProgress>
)

@Serializable
data class CreateTrainingDto(
        val finishDateInMillis: String,
        val exercisesWithProgress: List<CreateTrainingExerciseWithProgressDto>
)

data class CreateTrainingExerciseWithProgress(
        val exerciseId: Int,
        val series: List<Series>
)

@Serializable
data class CreateTrainingExerciseWithProgressDto(
        val exerciseId: Int,
        val series: List<SeriesDto>
)

fun CreateTrainingDto.toDomain() = CreateTraining(
finishDateInMillis = LocalDateTime.parse(finishDateInMillis),
exercisesWithProgress = exercisesWithProgress.map { it.toDomain() },
)

fun CreateTrainingExerciseWithProgressDto.toDomain() = CreateTrainingExerciseWithProgress(
        exerciseId = exerciseId,
        series = series.map { it.toDomain() }
)