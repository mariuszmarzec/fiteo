package com.marzec.repositories

import com.marzec.database.UserEntity
import com.marzec.database.UserTable
import com.marzec.database.dbCall
import com.marzec.model.domain.User
import org.jetbrains.exposed.sql.transactions.transaction

class UserRepositoryImpl : UserRepository {

    override fun checkPassword(email: String, password: String): Boolean {
        return dbCall {
            UserEntity.find {
                UserTable.email eq email
            }.first().password == password
        }
    }

    override fun createUser(email: String, password: String): User {
        return dbCall {
            UserEntity.new {
                this.email = email
                this.password = password
            }
        }.toDomain()
    }

    override fun getUser(id: Int): User {
        return dbCall {
            UserEntity[id]
        }.toDomain()
    }

    override fun getUser(email: String): User {
        return dbCall {
            UserEntity.find { UserTable.email eq email }
                    .first().let {
                        User(it.id.value, it.email)
                    }
        }
    }
}