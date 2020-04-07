package com.marzec.model.domain

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
        val repsNumber: Int
)