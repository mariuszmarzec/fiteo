package com.marzec.fiteo.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequestDto(
        val email: String,
        val password: String,
        val repeatedPassword: String
)
