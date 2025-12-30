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

class FcmServiceImpl(
    private val fcmTokenRepository: FcmTokenRepository
) : FcmService {
    override fun addToken(userId: Int, fcmToken: String, platform: String?): FcmToken {
        return fcmTokenRepository.addToken(userId, fcmToken, platform)
    }

    override fun deleteToken(userId: Int, fcmToken: String) {
        fcmTokenRepository.deleteToken(userId, fcmToken)
    }

    override fun deleteTokensForUser(userId: Int) {
        fcmTokenRepository.deleteTokensForUser(userId)
    }

    override fun sendPushNotification(userId: Int, taskDto: TaskDto) {
        // No-op in common code, implementation will be in JVM
    }
}
