package com.marzec.model.domain

data class Training(
        val id: String,
        val userId: String,
        val templateId: String,
        val createDateInMillis: Long,
        val finishDateInMillis: Long,
        val exercisesWithProgress: List<TrainingExerciseWithProgress>
)

data class TrainingDto(
        val id: String,
        val userId: String,
        val templateId: String,
        val createDateInMillis: Long,
        val finishDateInMillis: Long,
        val exercisesWithProgress: List<TrainingExerciseWithProgressDto>
)

fun Training.toDto() = TrainingDto(
id = id,
userId = userId,
templateId = templateId,
createDateInMillis = createDateInMillis,
finishDateInMillis = finishDateInMillis,
exercisesWithProgress = exercisesWithProgress.map { it.toDto() }
)