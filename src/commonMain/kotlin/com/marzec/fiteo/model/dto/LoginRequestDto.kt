package com.marzec.fiteo.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDto(
    val email: String?,
    val password: String?
)
