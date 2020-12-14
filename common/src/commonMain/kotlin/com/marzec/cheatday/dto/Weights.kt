package com.marzec.cheatday.dto

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