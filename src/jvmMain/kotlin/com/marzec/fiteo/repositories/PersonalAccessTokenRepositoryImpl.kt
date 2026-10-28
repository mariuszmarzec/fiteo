package com.marzec.fiteo.repositories

import com.marzec.core.PatTimeProvider
import com.marzec.core.UtcPatTimeProvider
import com.marzec.database.PersonalAccessTokenEntity
import com.marzec.database.PersonalAccessTokenTable
import com.marzec.database.UserEntity
import com.marzec.database.dbCall
import com.marzec.fiteo.model.domain.PersonalAccessToken
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDateTime
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database

class PersonalAccessTokenRepositoryImpl(
    private val database: Database,
    private val timeProvider: PatTimeProvider = UtcPatTimeProvider()
) : PersonalAccessTokenRepository {

    override fun createToken(userId: Int, name: String, tokenHash: String, tokenPrefix: String, expiresAt: LocalDateTime?, createdAt: LocalDateTime?): PersonalAccessToken = database.dbCall {
        PersonalAccessTokenEntity.new {
            user = UserEntity[userId]
            this.name = name
            this.tokenHash = tokenHash
            this.tokenPrefix = tokenPrefix
            this.createdAt = (createdAt ?: timeProvider.currentTime()).toJavaLocalDateTime()
            this.expiresAt = expiresAt?.toJavaLocalDateTime()
            this.lastUsedAt = null
        }.toDomain()
    }

    override fun getTokensForUser(userId: Int): List<PersonalAccessToken> = database.dbCall {
        PersonalAccessTokenEntity.find { PersonalAccessTokenTable.userId eq userId }
            .map { it.toDomain() }
    }

    override fun findByTokenHash(tokenHash: String): PersonalAccessToken? = database.dbCall {
        PersonalAccessTokenEntity.find { PersonalAccessTokenTable.tokenHash eq tokenHash }
            .firstOrNull()?.toDomain()
    }

    override fun updateLastUsedAt(tokenId: Int) {
        database.dbCall {
            PersonalAccessTokenEntity.findById(tokenId)?.let {
                it.lastUsedAt = timeProvider.currentTime().toJavaLocalDateTime()
            }
        }
    }

    override fun deleteToken(userId: Int, tokenId: Int) {
        database.dbCall {
            PersonalAccessTokenEntity.find { PersonalAccessTokenTable.id eq tokenId }
                .firstOrNull()
                ?.takeIf { it.user.id.value == userId }
                ?.delete()
        }
    }
}
