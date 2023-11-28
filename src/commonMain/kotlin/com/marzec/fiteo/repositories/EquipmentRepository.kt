package com.marzec.fiteo.repositories

import com.marzec.fiteo.model.domain.Equipment
import com.marzec.fiteo.model.domain.UpdateEquipment

interface EquipmentRepository {

    fun getAll(): List<Equipment>

    fun getById(id: String): Equipment

    fun addAll(equipment: List<Equipment>)

    fun create(equipment: Equipment): Equipment

    fun update(id: String, update: UpdateEquipment): Equipment
    fun delete(id: String): Equipment
}
