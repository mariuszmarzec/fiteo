@file:UseSerializers(LocalDateTimeSerializer::class)

package com.marzec.fiteo.model.dto

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class TrainingResponseDto(
    val id: Long,
    val name: String,
    val exercises: List<ExerciseDto>,
    val date: LocalDateTime
)