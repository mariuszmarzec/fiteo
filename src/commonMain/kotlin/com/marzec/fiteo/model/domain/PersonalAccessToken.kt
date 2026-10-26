package com.marzec.fiteo.model.domain

import kotlinx.datetime.LocalDateTime

data class PersonalAccessToken(
    val id: Int,
    val userId: Int,
    val name: String,
    val tokenHash: String,
    val tokenPrefix: String,
    val createdAt: LocalDateTime,
    val expiresAt: LocalDateTime?,
    val lastUsedAt: LocalDateTime?
)
