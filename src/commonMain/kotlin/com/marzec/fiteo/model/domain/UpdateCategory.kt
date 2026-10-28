package com.marzec.fiteo.model.domain

import com.marzec.extensions.getByProperty
import kotlinx.serialization.json.JsonElement

data class UpdateCategory(val name: String?)

fun Map<String, JsonElement?>.toUpdateCategory(): UpdateCategory = UpdateCategory(
    name = getByProperty(UpdateCategory::name),
)
