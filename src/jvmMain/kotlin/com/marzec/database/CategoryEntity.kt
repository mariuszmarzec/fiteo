package com.marzec.database

import com.marzec.fiteo.model.domain.Category
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column

object CategoryTable : IdTable<String>("categories") {

    private const val ID_LENGTH = 36
    private const val NAME_LENGTH = 100

    override val id: Column<EntityID<String>> = varchar("id", ID_LENGTH).entityId()

    val name = varchar("name", NAME_LENGTH)

    override val primaryKey by lazy { super.primaryKey ?: PrimaryKey(id) }
}

class CategoryEntity(id: EntityID<String>): Entity<String>(id) {
    var name by CategoryTable.name

    fun toDomain() = Category(id.value, name)

    companion object : EntityClass<String, CategoryEntity>(CategoryTable)
}
