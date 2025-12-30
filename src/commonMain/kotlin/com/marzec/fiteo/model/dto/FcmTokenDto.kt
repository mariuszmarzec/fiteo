package com.marzec.fiteo.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class FcmTokenDto(
    val id: Int,
    val userId: Int,
    val fcmToken: String,
    val platform: String?,
    val updatedAt: String
)

@Serializable
data class CreateFcmTokenDto(
    val fcmToken: String,
    val platform: String? = null
)
