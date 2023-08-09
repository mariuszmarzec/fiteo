package com.marzec.fiteo.repositories

import com.marzec.database.UserEntity
import com.marzec.database.UserTable
import com.marzec.database.dbCall
import com.marzec.fiteo.model.domain.User
import org.jetbrains.exposed.sql.Database

class UserRepositoryImpl(private val database: Database) : UserRepository {

    override fun checkPassword(email: String, password: String): Boolean {
        return database.dbCall {
            UserEntity.find {
                UserTable.email eq email
            }.first().password == password
        }
    }

    override fun createUser(email: String, password: String): User {
        return database.dbCall {
            UserEntity.new {
                this.email = email
                this.password = password
            }
        }.toDomain()
    }

    override fun getUser(id: Int): User {
        return database.dbCall {
            UserEntity[id]
        }.toDomain()
    }

    override fun getUser(email: String): User {
        return database.dbCall {
            UserEntity.find { UserTable.email eq email }
                    .first().let {
                        User(it.id.value, it.email)
                    }
        }
    }

    override fun getUsers(): List<User> = database.dbCall {
        UserEntity.all().map { User(it.id.value, it.email) }
    }
}
