package com.marzec.database

import com.marzec.fiteo.model.dto.UserDto
import io.ktor.auth.Principal

data class UserPrincipal(val id: Int, val email: String): Principal

fun UserDto.toPrincipal() = UserPrincipal(id, email)
