package com.marzec.fiteo.model.domain

import com.marzec.extensions.formatDate
import com.marzec.fiteo.model.dto.ExerciseDto
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

data class TrainingExerciseWithProgress(
    val id: Int,
    val exercise: Exercise,
    val series: List<Series>,
    val templatePart: TrainingTemplatePart?,
    val name: String
)

data class Series(
    val seriesId: Int,
    val exerciseId: Int,
    val trainingId: Int,
    val date: LocalDateTime,
    val burden: Float?,
    val timeInMillis: Long?,
    val repsNumber: Int?,
    val note: String
)

@Serializable
data class TrainingExerciseWithProgressDto(
    val id: Int,
    val exercise: ExerciseDto,
    val series: List<SeriesDto>,
    val templatePartId: Int?,
    val name: String
)

@Serializable
data class SeriesDto(
    val seriesId: Int,
    val exerciseId: Int,
    val trainingId: Int,
    val date: String,
    val burden: Float?,
    val timeInMillis: Long?,
    val repsNumber: Int?,
    val note: String
)

fun TrainingExerciseWithProgress.toDto() = TrainingExerciseWithProgressDto(
    id = id,
    exercise = exercise.toDto(),
    series = series.map { it.toDto() },
    templatePartId = templatePart?.id,
    name = name
)

fun Series.toDto() = SeriesDto(
    seriesId = seriesId,
    exerciseId = exerciseId,
    trainingId = trainingId,
    date = date.formatDate(),
    burden = burden,
    timeInMillis = timeInMillis,
    repsNumber = repsNumber,
    note = note
)

fun SeriesDto.toDomain() = Series(
    seriesId = seriesId,
    exerciseId = exerciseId,
    trainingId = trainingId,
    date = LocalDateTime.parse(date),
    burden = burden,
    timeInMillis = timeInMillis,
    repsNumber = repsNumber,
    note = note
)
