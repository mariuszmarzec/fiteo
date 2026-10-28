package com.marzec.fiteo.model.domain

import com.marzec.extensions.getByProperty
import kotlinx.serialization.json.JsonElement

data class UpdateEquipment(val name: String?)

fun Map<String, JsonElement?>.toUpdateEquipment(): UpdateEquipment = UpdateEquipment(
    name = getByProperty(UpdateEquipment::name),
)
