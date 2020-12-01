package com.marzec.cheatday.domain

import com.marzec.cheatday.dto.WeightDto
import kotlinx.datetime.LocalDateTime

data class Weight(
        val id: Int,
        val value: Float,
        val date: LocalDateTime
)

fun Weight.toDto() = WeightDto(
        id,
        value,
        date.toString()
)