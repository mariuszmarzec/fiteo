package com.marzec.core.entity

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable

abstract class CommonEntity<
        ID : Comparable<ID>,
        DOMAIN,
        >(id: EntityID<ID>) : Entity<ID>(id) {

    abstract fun toDomain(): DOMAIN
}

abstract class CommonEntityClass<ID : Comparable<ID>, DOMAIN, CREATE, UPDATE, ENTITY : CommonEntity<ID, DOMAIN>>(
    table: IdTable<ID>,
    private val entityType: Class<ENTITY>? = null,
    entityCtor: ((EntityID<ID>) -> ENTITY)? = null
) : EntityClass<ID, ENTITY>(table, entityType, entityCtor) {

    abstract fun create(item: CREATE): ENTITY

    abstract fun update(id: ID, update: UPDATE): ENTITY

    fun findByIdOrThrow(id: ID): ENTITY =
        findById(id) ?: throw NoSuchElementException(
            "No ${
                entityType?.name?.let { "$it " }.orEmpty()
            }result with id: $id"
        )
}