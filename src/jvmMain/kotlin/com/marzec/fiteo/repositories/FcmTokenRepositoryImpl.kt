package com.marzec.fiteo.repositories

import com.marzec.core.currentTime
import com.marzec.database.FcmTokenEntity
import com.marzec.database.FcmTokenTable
import com.marzec.database.UserEntity
import com.marzec.database.dbCall
import com.marzec.fiteo.model.domain.FcmToken
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.and
import java.time.LocalDateTime

class FcmTokenRepositoryImpl(private val database: Database) : FcmTokenRepository {

    override fun getTokensForUser(userId: Int): List<FcmToken> = database.dbCall {
        FcmTokenEntity.find { FcmTokenTable.userId eq userId }
            .map { it.toDomain() }
    }

    override fun addToken(userId: Int, fcmToken: String, platform: String?): FcmToken = database.dbCall {
        val existingToken = FcmTokenEntity.find { FcmTokenTable.fcmToken eq fcmToken }.firstOrNull()
        
        if (existingToken != null) {
            existingToken.user = UserEntity[userId]
            existingToken.platform = platform
            existingToken.updatedAt = currentTime().toJavaLocalDateTime()
            existingToken.toDomain()
        } else {
            FcmTokenEntity.new {
                this.user = UserEntity[userId]
                this.fcmToken = fcmToken
                this.platform = platform
                this.updatedAt = currentTime().toJavaLocalDateTime()
            }.toDomain()
        }
    }

    override fun deleteToken(userId: Int, fcmToken: String) = database.dbCall {
        FcmTokenEntity.find { (FcmTokenTable.userId eq userId) and (FcmTokenTable.fcmToken eq fcmToken) }
            .forEach { it.delete() }
    }

    override fun deleteTokensForUser(userId: Int) = database.dbCall {
        FcmTokenEntity.find { FcmTokenTable.userId eq userId }
            .forEach { it.delete() }
    }

    private fun FcmTokenEntity.toDomain() = FcmToken(
        id = id.value,
        userId = user.id.value,
        fcmToken = fcmToken,
        platform = platform,
        updatedAt = updatedAt.toKotlinLocalDateTime()
    )
}
