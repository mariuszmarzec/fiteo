package com.marzec.cheatday.domain

import com.marzec.cheatday.dto.WeightDto
import com.marzec.extensions.formatDate
import com.marzec.extensions.getByProperty
import kotlinx.serialization.json.JsonElement
import kotlinx.datetime.LocalDateTime

data class Weight(
        val id: Int,
        val value: Float,
        val date: LocalDateTime
)

fun Weight.toDto() = WeightDto(
        id,
        value,
        date.formatDate()
)

data class CreateWeight(val weight: Float, val date: LocalDateTime)

data class UpdateWeight(
        val value: Float?,
        val date: String?
)

fun Map<String, JsonElement?>.toUpdateWeight(): UpdateWeight = UpdateWeight(
        value = getByProperty(UpdateWeight::value),
        date = getByProperty(UpdateWeight::date)
)
