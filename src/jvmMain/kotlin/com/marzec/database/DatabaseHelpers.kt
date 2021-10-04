package com.marzec.database

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.sql.SizedCollection

inline fun <reified ENTITY: IntEntity> IntEntityClass<ENTITY>.findByIdOrThrow(id: Int): ENTITY =
        findById(id) ?: throw NoSuchElementException("No ${ENTITY::class.simpleName} result with id: $id")

inline fun <reified ENTITY: Entity<String>> EntityClass<String, ENTITY>.findByIdOrThrow(id: String): ENTITY =
        findById(id) ?: throw NoSuchElementException("No ${ENTITY::class.simpleName} result with id: $id")

fun <T> List<T>.toSized(): SizedCollection<T> = SizedCollection(this)
