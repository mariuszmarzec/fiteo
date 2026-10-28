package com.marzec.fiteo.model.domain

import com.marzec.extensions.formatDate
import com.marzec.fiteo.model.dto.FcmTokenDto
import kotlinx.datetime.LocalDateTime

data class FcmToken(
    val id: Int,
    val userId: Int,
    val fcmToken: String,
    val platform: String?,
    val updatedAt: LocalDateTime
)

fun FcmToken.toDto() = FcmTokenDto(
    id = id,
    userId = userId,
    fcmToken = fcmToken,
    platform = platform,
    updatedAt = updatedAt.formatDate()
)
