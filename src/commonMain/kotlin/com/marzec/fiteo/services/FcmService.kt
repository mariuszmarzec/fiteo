package com.marzec.fiteo.services

import com.marzec.fiteo.model.domain.FcmToken
import com.marzec.fiteo.repositories.FcmTokenRepository
import com.marzec.todo.dto.TaskDto

interface FcmService {
    fun addToken(userId: Int, fcmToken: String, platform: String?): FcmToken
    fun deleteToken(userId: Int, fcmToken: String)
    fun deleteTokensForUser(userId: Int)
    fun sendPushNotification(userId: Int, taskDto: TaskDto)
}
