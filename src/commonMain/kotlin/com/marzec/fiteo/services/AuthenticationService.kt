package com.marzec.fiteo.services

import com.marzec.exceptions.HttpException
import com.marzec.exceptions.HttpStatus
import com.marzec.fiteo.model.domain.User
import com.marzec.fiteo.repositories.UserRepository

interface AuthenticationService {

    fun checkPassword(email: String, password: String): User
    fun getUser(id: Int): User
    fun register(email: String, password: String, repeatedPassword: String): User
}

class AuthenticationServiceImpl(
        private val userRepository: UserRepository
) : AuthenticationService {

    override fun checkPassword(email: String, password: String): User {
        return try {
            if (userRepository.checkPassword(email, password)) {
                userRepository.getUser(email)
            } else {
                throw HttpException("Wrong password", HttpStatus.BAD_REQUEST)
            }
        } catch (_: NoSuchElementException) {
            throw HttpException("User with email: $email not found", HttpStatus.NOT_FOUND)
        }
    }

    override fun getUser(id: Int): User = userRepository.getUser(id)

    override fun register(email: String, password: String, repeatedPassword: String): User {

        if (email.isBlank() || email.isEmpty()) {
            throw HttpException("Empty email", HttpStatus.BAD_REQUEST)
        }
        if (password.length < MIN_PASSWORD_LENGTH) {
            throw HttpException("Password too short, min is $MIN_PASSWORD_LENGTH", HttpStatus.BAD_REQUEST)
        }
        if (password != repeatedPassword) {
            throw HttpException("Passwords are different", HttpStatus.BAD_REQUEST)
        }
        return userRepository.createUser(email, password)
     }

    companion object {
        private const val MIN_PASSWORD_LENGTH = 6
    }
}
