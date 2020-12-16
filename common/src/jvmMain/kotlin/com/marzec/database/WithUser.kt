package com.marzec.database

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.id.EntityID

abstract class IntEntityWithUser(id: EntityID<Int>) : IntEntity(id) {

    abstract var user: UserEntity

    fun belongsToUserOrThrow(userId: Int) {
        if (this.user.id.value != userId) {
            throw NoSuchElementException("No ${this::class.simpleName} with id: $id. for user with id: $userId")
        }
    }

    fun deleteIfBelongsToUserOrThrow(userId: Int) {
        if (this.user.id.value != userId) {
            throw NoSuchElementException("No ${this::class.simpleName} with id: $id. for user with id: $userId")
        }
        delete()
    }
}