package com.marzec.fiteo.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(val id: Int, val email: String)