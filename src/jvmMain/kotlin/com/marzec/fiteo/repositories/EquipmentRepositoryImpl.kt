package com.marzec.fiteo.repositories

import com.marzec.database.EquipmentEntity
import com.marzec.database.EquipmentTable
import com.marzec.database.dbCall
import com.marzec.database.findByIdOrThrow
import com.marzec.fiteo.model.domain.Equipment
import com.marzec.fiteo.model.domain.UpdateEquipment
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.insert

class EquipmentRepositoryImpl(private val database: Database) : EquipmentRepository {
    override fun getAll(): List<Equipment> = database.dbCall {
        EquipmentEntity.all().orderBy(EquipmentTable.name to SortOrder.ASC).map { it.toDomain() }
    }

    override fun getById(id: String): Equipment = database.dbCall {
        EquipmentEntity.findByIdOrThrow(id).toDomain()
    }

    override fun addAll(equipment: List<Equipment>) = database.dbCall {
        equipment.forEach { equipment ->
            EquipmentTable.insert {
                it[id] = equipment.id
                it[name] = equipment.name
            }
        }
    }

    override fun create(equipment: Equipment): Equipment = database.dbCall {
        EquipmentEntity.new(equipment.id) {
            name = equipment.name
        }
    }.toDomain()

    override fun update(id: String, update: UpdateEquipment): Equipment = database.dbCall {
        EquipmentEntity.findByIdOrThrow(id).apply {
            com.marzec.extensions.update(this::name, update.name)
        }
    }.toDomain()

    override fun delete(id: String): Equipment = database.dbCall {
        val entity = EquipmentEntity.findByIdOrThrow(id)
        entity.delete()
        entity.toDomain()
    }
}
