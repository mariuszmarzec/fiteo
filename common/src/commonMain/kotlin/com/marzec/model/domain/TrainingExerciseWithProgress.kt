package com.marzec.model.domain

import com.marzec.model.dto.ExerciseDto

data class TrainingExerciseWithProgress(
        val exercise: Exercise,
        val series: List<Series>
)

data class Series(
        val seriesId: String,
        val exerciseId: String,
        val trainingId: String,
        val date: Long,
        val burden: Int,
        val timeInMillis: Long,
        val repsNumber: Int,
        val note: String
)

data class TrainingExerciseWithProgressDto(
        val exercise: ExerciseDto,
        val series: List<SeriesDto>
)

data class SeriesDto(
        val seriesId: String,
        val exerciseId: String,
        val trainingId: String,
        val date: Long,
        val burden: Int,
        val timeInMillis: Long,
        val repsNumber: Int,
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
        date = date,
        burden = burden,
        timeInMillis = timeInMillis,
        repsNumber = repsNumber,
        note = note
)