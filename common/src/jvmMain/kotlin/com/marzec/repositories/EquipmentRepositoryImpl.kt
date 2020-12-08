package com.marzec.repositories

import com.marzec.database.EquipmentEntity
import com.marzec.database.EquipmentTable
import com.marzec.database.dbCall
import com.marzec.model.domain.Equipment
import org.jetbrains.exposed.sql.insert

class EquipmentRepositoryImpl : EquipmentRepository {
    override fun getAll(): List<Equipment> = dbCall {
        EquipmentEntity.all().map { it.toDomain() }
    }

    override fun addAll(equipment: List<Equipment>) = dbCall {
        equipment.forEach { equipment ->
            EquipmentTable.insert {
                it[id] = equipment.id
                it[name] = equipment.name
            }
        }
    }
}