package com.marzec.core.model.dto

import com.marzec.core.model.domain.FeatureToggle
import com.marzec.core.model.domain.NewFeatureToggle
import kotlinx.serialization.Serializable

@Serializable
data class FeatureToggleDto(
    val id: Int,
    val name: String,
    val value: String
)

@Serializable
data class NewFeatureToggleDto(val name: String, val value:String)

fun FeatureToggleDto.toDomain() = FeatureToggle(id, name, value)

fun NewFeatureToggleDto.toDomain() = NewFeatureToggle(name, value)