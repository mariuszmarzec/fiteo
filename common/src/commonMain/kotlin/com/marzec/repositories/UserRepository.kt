package com.marzec.repositories

interface UserRepository {

    fun checkPassword(email: String, password: String): Boolean
}