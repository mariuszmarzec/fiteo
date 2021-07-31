package com.marzec.database

import com.marzec.fiteo.model.domain.Equipment
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column

object EquipmentTable : IdTable<String>("equipment") {

    override val id: Column<EntityID<String>> = varchar("id", 36).entityId()

    val name = varchar("name", 300)

    override val primaryKey by lazy { super.primaryKey ?: PrimaryKey(id) }
}

class EquipmentEntity(id: EntityID<String>): Entity<String>(id) {
    var name by EquipmentTable.name

    fun toDomain() = Equipment(id.value, name)

    companion object : EntityClass<String, EquipmentEntity>(EquipmentTable)
}
