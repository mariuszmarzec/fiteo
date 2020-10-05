package com.marzec.database

import com.marzec.model.domain.User
import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object UserTable : IntIdTable("users") {
    val email = varchar("email", 100)
    val password = varchar("password", 32)

    init {
        index(true, email)
    }
}

class UserEntity(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<UserEntity>(UserTable)
    var email by UserTable.email
    var password by UserTable.password

    override fun toString(): String {
        return "${id._value} $email $password"
    }

    fun toDomain() = User(id.value, email)
}
