package com.marzec.exercises

import com.marzec.model.domain.Request
import com.marzec.model.domain.User
import com.marzec.repositories.UserRepository

interface AuthenticationService {

    fun checkPassword(email: String, password: String): Request<User>
    fun getUser(id: Int): Request<User>
}

class AuthenticationServiceImpl(
        private val userRepository: UserRepository
) : AuthenticationService {

    override fun checkPassword(email: String, password: String): Request<User> {
        return try {
            if (userRepository.checkPassword(email, password)) {
               Request.Success(userRepository.getUser(email))
            } else {
                Request.Error("Wrong password")
            }
        } catch (e: NoSuchElementException) {
            Request.Error("User with email: $email not found", 404)
        } catch (e: Exception) {
            e.printStackTrace()
            Request.Error(e.message.toString())
        }
    }

    override fun getUser(id: Int): Request<User> {
        return try {
            Request.Success(userRepository.getUser(id))
        } catch (e: NoSuchElementException) {
            Request.Error("User with id: $id not found", 404)
        } catch (e: Exception) {
            e.printStackTrace()
            Request.Error(e.message.toString())
        }
    }
}