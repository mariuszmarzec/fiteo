package com.marzec.model.domain

import com.marzec.model.dto.UserDto

data class User(val id: Int, val email: String)

fun User.toDto() = UserDto(id, email)