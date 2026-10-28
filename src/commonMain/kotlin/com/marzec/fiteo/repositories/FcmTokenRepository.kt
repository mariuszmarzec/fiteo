package com.marzec.fiteo.repositories

import com.marzec.fiteo.model.domain.FcmToken

interface FcmTokenRepository {
    fun getTokensForUser(userId: Int): List<FcmToken>
    fun addToken(userId: Int, fcmToken: String, platform: String?): FcmToken
    fun deleteToken(userId: Int, fcmToken: String)
    fun deleteTokensForUser(userId: Int)
}
