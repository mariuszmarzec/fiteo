package com.marzec.database

import com.marzec.core.entity.CommonEntity
import com.marzec.core.entity.CommonEntityClass
import com.marzec.extensions.update
import com.marzec.fiteo.model.domain.Category
import com.marzec.fiteo.model.domain.UpdateCategory
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

class CategoryEntity(id: EntityID<String>) :
    CommonEntity<String, Category>(id) {
    var name by CategoryTable.name

    override fun toDomain() = Category(id.value, name)

    companion object : CommonEntityClass<String, Category, Category, UpdateCategory, CategoryEntity>(
        CategoryTable,
        CategoryEntity::class.java
    ) {
        override fun create(item: Category): CategoryEntity =
            new(item.id) {
                name = item.name
            }

        override fun update(id: String, update: UpdateCategory): CategoryEntity =
            findByIdOrThrow(id).apply {
                update(this::name, update.name)
            }
    }
}