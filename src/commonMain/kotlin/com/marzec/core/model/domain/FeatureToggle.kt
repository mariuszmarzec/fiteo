package com.marzec.core.model.domain

import com.marzec.core.model.dto.FeatureToggleDto
import com.marzec.extensions.getByProperty
import kotlinx.serialization.json.JsonElement

data class FeatureToggle(
    val id: Int,
    val name: String,
    val value: String
)

data class NewFeatureToggle(
    val name: String,
    val value: String
)

data class UpdateFeatureToggle(
    val name: String?,
    val value: String?
)

fun FeatureToggle.toDto() = FeatureToggleDto(
    id, name, value
)

fun Map<String, JsonElement?>.toUpdateFeatureToggle(): UpdateFeatureToggle = UpdateFeatureToggle(
    name = getByProperty(UpdateFeatureToggle::name),
    value = getByProperty(UpdateFeatureToggle::value)
)
