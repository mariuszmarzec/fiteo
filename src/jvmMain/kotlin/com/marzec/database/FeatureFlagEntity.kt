package com.marzec.database

import com.marzec.core.entity.CommonEntity
import com.marzec.core.entity.CommonEntityClass
import com.marzec.core.model.domain.FeatureToggle
import com.marzec.core.model.domain.NewFeatureToggle
import com.marzec.core.model.domain.UpdateFeatureToggle
import com.marzec.extensions.update
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column

object FeatureToggleTable : IdTable<Int>("feature_toggles") {

    private const val NAME_LENGTH = 100

    override val id: Column<EntityID<Int>> = integer("id").entityId().autoIncrement()

    val name = varchar("name", NAME_LENGTH)
    val value = varchar("value", NAME_LENGTH)

    override val primaryKey by lazy { super.primaryKey ?: PrimaryKey(id) }
}

class FeatureToggleEntity(id: EntityID<Int>) :
    CommonEntity<Int, FeatureToggle>(id) {
    var name by FeatureToggleTable.name
    var value by FeatureToggleTable.value

    override fun toDomain() = FeatureToggle(id.value, name, value)

    companion object : CommonEntityClass<Int, FeatureToggle, NewFeatureToggle, UpdateFeatureToggle, FeatureToggleEntity>(
        FeatureToggleTable,
        FeatureToggleEntity::class.java
    ) {
        override fun create(item: NewFeatureToggle): FeatureToggleEntity =
            new {
                name = item.name
                value = item.value
            }

        override fun update(id: Int, update: UpdateFeatureToggle): FeatureToggleEntity =
            findByIdOrThrow(id).apply {
                update(this::name, update.name)
                update(this::value, update.value)
            }
    }
}