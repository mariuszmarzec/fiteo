package com.marzec.repositories

import com.marzec.model.domain.User

interface UserRepository {

    fun checkPassword(email: String, password: String): Boolean
    fun getUser(email: String): User
    fun getUser(id: Int): User
}