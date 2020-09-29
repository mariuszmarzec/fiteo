package com.marzec.repositories

import com.marzec.database.UserEntity
import com.marzec.database.UserTable
import org.jetbrains.exposed.sql.transactions.transaction

class UserRepositoryImpl : UserRepository {

    override fun checkPassword(email: String, password: String): Boolean = transaction {
        val users = UserEntity.find {
            UserTable.email eq email
        }
        val userEntity = users.first()
        userEntity.password == password
    }
}