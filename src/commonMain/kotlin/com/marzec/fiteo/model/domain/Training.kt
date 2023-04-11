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

data class UpdateTraining(
    val finishDateInMillis: LocalDateTime,
    val exercisesWithProgress: List<UpdateTrainingExerciseWithProgress>
)

@Serializable
data class UpdateTrainingDto(
    val finishDateInMillis: String,
    val exercisesWithProgress: List<UpdateTrainingExerciseWithProgressDto>
)

data class UpdateTrainingExerciseWithProgress(
    val exerciseId: Int,
    val series: List<Series>,
    val trainingPartId: Int?
)

@Serializable
data class UpdateTrainingExerciseWithProgressDto(
    val exerciseId: Int,
    val series: List<SeriesDto>,
    val trainingPartId: Int? = null
)

fun UpdateTrainingDto.toDomain() = UpdateTraining(
    finishDateInMillis = LocalDateTime.parse(finishDateInMillis),
    exercisesWithProgress = exercisesWithProgress.map { it.toDomain() },
)

fun UpdateTrainingExerciseWithProgressDto.toDomain() = UpdateTrainingExerciseWithProgress(
    exerciseId = exerciseId,
    series = series.map { it.toDomain() },
    trainingPartId = trainingPartId
)
