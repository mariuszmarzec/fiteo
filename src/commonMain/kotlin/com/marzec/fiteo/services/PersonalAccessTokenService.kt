package com.marzec.fiteo.services

import com.marzec.core.SecureRandom
import com.marzec.core.sha256Hex
import com.marzec.core.currentTime
import com.marzec.exceptions.HttpException
import com.marzec.exceptions.HttpStatus
import com.marzec.fiteo.model.domain.PersonalAccessToken
import com.marzec.fiteo.model.domain.TokenExpiration
import com.marzec.fiteo.repositories.PersonalAccessTokenRepository
import com.marzec.fiteo.repositories.UserRepository
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.plus

private const val TOKEN_LENGTH_BYTES = 32
private const val TOKEN_PREFIX_LENGTH = 8
private const val HEX_CHARS = "0123456789abcdef"

/**
 * Domain service for generating, persisting and verifying Personal Access Tokens.
 *
 * The raw secret is generated with a cryptographically strong random source and is
 * returned to the caller only as part of the [PersonalAccessTokenCreation] result.
 * Only the SHA-256 hash of the secret plus a non-secret prefix and the token name are
 * ever persisted. Verification is performed by hash lookup, expiration check and
 * [lastUsedAt] update — the raw secret is never stored, logged or compared directly.
 */
interface PersonalAccessTokenService {

    fun create(userId: Int, name: String, expiration: TokenExpiration): PersonalAccessTokenCreation

    fun verify(token: String): PersonalAccessToken

    fun getTokensForUser(userId: Int): List<PersonalAccessToken>

    fun deleteToken(userId: Int, tokenId: Int)
}

/**
 * Result of creating a token. [token] is the raw secret that must be shown to the
 * caller exactly once; [pat] is the persisted record (hash, prefix, name, owner).
 */
data class PersonalAccessTokenCreation(
    val token: String,
    val pat: PersonalAccessToken
)

class PersonalAccessTokenServiceImpl(
    private val secureRandom: SecureRandom,
    private val patRepository: PersonalAccessTokenRepository,
    private val userRepository: UserRepository
) : PersonalAccessTokenService {

    override fun create(userId: Int, name: String, expiration: TokenExpiration): PersonalAccessTokenCreation {
        val secret = generateSecret()
        val tokenHash = sha256Hex(secret.encodeToByteArray())
        val tokenPrefix = tokenHash.take(TOKEN_PREFIX_LENGTH)
        val expiresAt = calculateExpiration(currentTime(), expiration)
        val pat = patRepository.createToken(userId, name, tokenHash, tokenPrefix, expiresAt)
        return PersonalAccessTokenCreation(token = secret, pat = pat)
    }

    override fun verify(token: String): PersonalAccessToken {
        val tokenHash = sha256Hex(token.encodeToByteArray())
        val stored = patRepository.findByTokenHash(tokenHash)
            ?: throw HttpException("Invalid token", HttpStatus.UNAUTHORIZED)
        val now = currentTime()
        if (stored.expiresAt != null && stored.expiresAt < now) {
            throw HttpException("Token expired", HttpStatus.UNAUTHORIZED)
        }
        patRepository.updateLastUsedAt(stored.id)
        return stored
    }

    override fun getTokensForUser(userId: Int): List<PersonalAccessToken> =
        patRepository.getTokensForUser(userId)

    override fun deleteToken(userId: Int, tokenId: Int) =
        patRepository.deleteToken(userId, tokenId)

    private fun generateSecret(): String {
        val bytes = secureRandom.nextBytes(TOKEN_LENGTH_BYTES)
        val hex = StringBuilder(bytes.size * 2)
        for (b in bytes) {
            val v = b.toInt() and 0xFF
            hex.append(HEX_CHARS[v shr 4])
            hex.append(HEX_CHARS[v and 0x0F])
        }
        return hex.toString()
    }

    private fun calculateExpiration(issuedAt: LocalDateTime, expiration: TokenExpiration): LocalDateTime? =
        when (expiration) {
            TokenExpiration.NEVER -> null
            TokenExpiration.THREE_MONTHS -> issuedAt.plusMonths(3)
            TokenExpiration.SIX_MONTHS -> issuedAt.plusMonths(6)
        }

    private fun LocalDateTime.plusMonths(months: Int): LocalDateTime =
        LocalDateTime(date.plus(months, DateTimeUnit.MONTH), time)
}