package com.marzec.fiteo.repositories

import com.marzec.fiteo.model.domain.PersonalAccessToken
import kotlinx.datetime.LocalDateTime

interface PersonalAccessTokenRepository {
    fun createToken(userId: Int, name: String, tokenHash: String, tokenPrefix: String, expiresAt: LocalDateTime?): PersonalAccessToken
    fun getTokensForUser(userId: Int): List<PersonalAccessToken>
    fun findByTokenHash(tokenHash: String): PersonalAccessToken?
    fun updateLastUsedAt(tokenId: Int)
    fun deleteToken(userId: Int, tokenId: Int)
}
