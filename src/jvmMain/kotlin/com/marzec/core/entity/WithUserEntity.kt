package com.marzec.core.entity

import com.marzec.database.IntEntityWithUser
import com.marzec.database.IntIdWithUserTable
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable

abstract class WithUserEntity<
        DOMAIN,
        >(id: EntityID<Int>) : IntEntityWithUser(id) {

    abstract fun toDomain(): DOMAIN
}

abstract class WithUserEntityClass<DOMAIN, CREATE, UPDATE, ENTITY : WithUserEntity<DOMAIN>>(
    val withUserTable: IntIdWithUserTable,
    private val entityType: Class<ENTITY>? = null,
    entityCtor: ((EntityID<Int>) -> ENTITY)? = null
) : IntEntityClass<ENTITY>(withUserTable, entityType, entityCtor) {

    abstract fun create(userId: Int, item: CREATE): ENTITY

    abstract fun update(userId: Int, entity: ENTITY, update: UPDATE): ENTITY

    fun findByIdOrThrow(id: Int): ENTITY =
        findById(id) ?: throw NoSuchElementException(
            "No ${
                entityType?.name?.let { "$it " }.orEmpty()
            }result with id: $id"
        )
}