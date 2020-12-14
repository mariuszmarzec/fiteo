package com.marzec.cheatday.dto

import com.marzec.cheatday.domain.Weight
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class WeightDto(
        val id: Int,
        val value: Float,
        val date: String
)

@Serializable
data class PutWeightDto(
        val value: Float,
        val date: String
)

fun WeightDto.toDomain() = Weight(
        id = id,
        value = value,
        date = LocalDateTime.parse(date)
)