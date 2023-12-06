package com.marzec.database

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.and

inline fun <reified ENTITY : IntEntity> IntEntityClass<ENTITY>.findByIdOrThrow(id: Int): ENTITY =
    findById(id) ?: throw NoSuchElementException("No ${ENTITY::class.simpleName} result with id: $id")

inline fun <reified ENTITY : IntEntityWithUser> IntEntityWithUserClass<ENTITY>.findByIdIfBelongsToUserOrThrow(
    userId: Int,
    id: Int
): ENTITY = find { table.id eq id and (withUserTable.userId eq userId) }.firstOrNull()
    ?: throw NoSuchElementException("No ${ENTITY::class.simpleName} result with id: $id for user with id: $userId")

inline fun <ID: Comparable<ID>, reified ENTITY : Entity<ID>> EntityClass<ID, ENTITY>.findByIdOrThrow(id: ID): ENTITY =
    findById(id) ?: throw NoSuchElementException("No ${ENTITY::class.simpleName} result with id: $id")

fun <T> List<T>.toSized(): SizedCollection<T> = SizedCollection(this)

abstract class IntEntityWithUserClass<out E : IntEntityWithUser>(
    val withUserTable: IntIdWithUserTable,
    entityType: Class<E>? = null
) : IntEntityClass<E>(withUserTable, entityType)

abstract class IntIdWithUserTable(name: String = "", columnName: String = "id") : IntIdTable(name, columnName) {

    abstract val userId: Column<EntityID<Int>>
}
