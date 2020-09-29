package com.marzec.exercises

import com.marzec.model.domain.Request
import com.marzec.repositories.UserRepository

interface AuthenticationService {

    fun checkPassword(email: String, password: String): Request<Unit>
}

class AuthenticationServiceImpl(
        private val userRepository: UserRepository
) : AuthenticationService {

    override fun checkPassword(email: String, password: String): Request<Unit> {
        return try {
            if (userRepository.checkPassword(email, password)) {
               Request.Success(Unit)
            } else {
                Request.Error("Wrong password")
            }
        } catch (e: NoSuchElementException) {
            Request.Error("User with email: $email not found", 404)
        } catch (e: Exception) {
            Request.Error(e.message.toString())
        }
    }
}