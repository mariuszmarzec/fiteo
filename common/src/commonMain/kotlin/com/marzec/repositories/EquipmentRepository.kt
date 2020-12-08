package com.marzec.repositories

import com.marzec.model.domain.Equipment

interface EquipmentRepository {

    fun getAll(): List<Equipment>

    fun addAll(equipment: List<Equipment>)
}