package com.marzec.fiteo.model.dto

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class CreatePersonalAccessTokenRequestDto(
    val name: String,
    val expiresIn: String
)

@Serializable
data class PersonalAccessTokenCreationDto(
    val id: Int,
    val name: String,
    val token: String,
    val prefix: String,
    val createdAt: LocalDateTime,
    val expiresAt: LocalDateTime?
)
