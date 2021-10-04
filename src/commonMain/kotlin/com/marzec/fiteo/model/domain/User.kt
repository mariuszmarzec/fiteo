package com.marzec.fiteo.model.domain

import com.marzec.fiteo.model.dto.UserDto

data class User(val id: Int, val email: String)

fun User.toDto() = UserDto(id, email)
