package com.marzec.fiteo.model.domain

import kotlinx.serialization.Serializable

sealed class Session

@Serializable
data class UserSession(
    val userId: Int,
    val timestamp: Long
) : Session()

@Serializable
data class TestUserSession(
    val userId: Int,
    val timestamp: Long
) : Session()
