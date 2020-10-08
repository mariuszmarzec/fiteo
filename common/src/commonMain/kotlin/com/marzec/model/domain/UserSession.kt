package com.marzec.model.domain

sealed class Session

data class UserSession(
        val userId: Int,
        var timestamp: Long
) : Session()
