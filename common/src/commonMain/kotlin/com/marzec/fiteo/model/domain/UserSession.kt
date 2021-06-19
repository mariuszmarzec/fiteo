package com.marzec.fiteo.model.domain

sealed class Session

data class UserSession(
    val userId: Int,
    val timestamp: Long
) : Session()

data class TestUserSession(
    val userId: Int,
    val timestamp: Long
) : Session()
