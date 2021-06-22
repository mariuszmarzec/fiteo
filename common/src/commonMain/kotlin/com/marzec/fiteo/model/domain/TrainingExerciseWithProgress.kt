package com.marzec.fiteo.model.domain

import com.marzec.fiteo.model.dto.ExerciseDto
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

data class TrainingExerciseWithProgress(
    val exercise: Exercise,
    val series: List<Series>
)

data class Series(
        val seriesId: Int,
        val exerciseId: Int,
        val trainingId: Int,
        val date: LocalDateTime,
        val burden: Int?,
        val timeInMillis: Long?,
        val repsNumber: Int?,
        val note: String
)

@Serializable
data class TrainingExerciseWithProgressDto(
    val exercise: ExerciseDto,
    val series: List<SeriesDto>
)

@Serializable
data class SeriesDto(
        val seriesId: Int,
        val exerciseId: Int,
        val trainingId: Int,
        val date: String,
        val burden: Int?,
        val timeInMillis: Long?,
        val repsNumber: Int?,
        val note: String
)

fun TrainingExerciseWithProgress.toDto() = TrainingExerciseWithProgressDto(
        exercise = exercise.toDto(),
        series = series.map { it.toDto() }
)

fun Series.toDto() = SeriesDto(
        seriesId = seriesId,
        exerciseId = exerciseId,
        trainingId = trainingId,
        date = date.toString(),
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
