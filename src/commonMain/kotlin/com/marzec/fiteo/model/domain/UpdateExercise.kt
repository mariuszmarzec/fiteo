package com.marzec.fiteo.model.domain

import com.marzec.extensions.getByProperty
import com.marzec.extensions.getNullableByProperty
import com.marzec.fiteo.model.dto.CategoryDto
import com.marzec.fiteo.model.dto.EquipmentDto
import com.marzec.fiteo.model.dto.toDomain
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

data class UpdateExercise(
    val name: String?,
    val animationUrl: NullableField<String>?,
    val videoUrl: NullableField<String>?,
    val category: List<Category>?,
    val neededEquipment: List<Equipment>?,
    val thumbnailUrl: NullableField<String>?
)

fun Map<String, JsonElement?>.toUpdateExercise(): UpdateExercise = UpdateExercise(
    name = getByProperty(UpdateExercise::name),
    animationUrl = getNullableByProperty(UpdateExercise::animationUrl),
    videoUrl = getNullableByProperty(UpdateExercise::videoUrl),
    category = getByProperty<List<Category>, List<CategoryDto>>(UpdateExercise::category) { it.map { it.toDomain() } },
    neededEquipment = getByProperty<List<Equipment>, List<EquipmentDto>>(UpdateExercise::neededEquipment) { it.map { it.toDomain() } },
    thumbnailUrl = getNullableByProperty(UpdateExercise::thumbnailUrl),
)
