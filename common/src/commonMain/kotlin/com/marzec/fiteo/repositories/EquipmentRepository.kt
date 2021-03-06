package com.marzec.fiteo.repositories

import com.marzec.fiteo.model.domain.Equipment

interface EquipmentRepository {

    fun getAll(): List<Equipment>

    fun addAll(equipment: List<Equipment>)
}