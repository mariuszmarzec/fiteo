package com.marzec.model.domain

data class Training(
        val id: String,
        val userId: String,
        val templateId: String,
        val createDateInMillis: Long,
        val finishDateInMillis: Long,
        val exercisesWithProgress: List<TrainingExerciseWithProgress>
)