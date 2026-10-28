package com.marzec.database

import com.marzec.fiteo.model.domain.User
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable

object UserTable : IntIdTable("users") {
    private const val ID_LENGTH = 100
    private const val PASSWORD_LENGTH = 32
    val email = varchar("email", ID_LENGTH)
    val password = varchar("password", PASSWORD_LENGTH)

    init {
        index(true, email)
    }
}

class UserEntity(id: EntityID<Int>): IntEntity(id) {
    var email by UserTable.email
    var password by UserTable.password

    override fun toString(): String {
        return "${id._value} $email $password"
    }

    fun toDomain() = User(id.value, email)

    companion object : IntEntityClass<UserEntity>(UserTable)
}
