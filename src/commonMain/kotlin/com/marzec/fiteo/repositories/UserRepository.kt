package com.marzec.fiteo.repositories

import com.marzec.fiteo.model.domain.User

interface UserRepository {

    fun checkPassword(email: String, password: String): Boolean
    fun getUser(email: String): User
    fun getUser(id: Int): User

    fun getUsers(): List<User>

    fun createUser(email: String, password: String): User
}
