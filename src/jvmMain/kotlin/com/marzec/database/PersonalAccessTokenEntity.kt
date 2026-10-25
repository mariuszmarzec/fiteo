package com.marzec.database

import com.marzec.core.currentTime
import com.marzec.fiteo.model.domain.PersonalAccessToken
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.javatime.datetime

object PersonalAccessTokenTable : IntIdTable("personal_access_tokens") {
    val userId = reference("user_id", UserTable)
    val name = varchar("name", 255)
    val tokenHash = varchar("token_hash", 64).uniqueIndex()
    val tokenPrefix = varchar("token_prefix", 16)
    val createdAt = datetime("created_at").default(currentTime().toJavaLocalDateTime())
    val expiresAt = datetime("expires_at").nullable()
    val lastUsedAt = datetime("last_used_at").nullable()

    init {
        index(false, userId)
    }
}

class PersonalAccessTokenEntity(id: EntityID<Int>) : IntEntity(id) {
    var user by UserEntity referencedOn PersonalAccessTokenTable.userId
    var name by PersonalAccessTokenTable.name
    var tokenHash by PersonalAccessTokenTable.tokenHash
    var tokenPrefix by PersonalAccessTokenTable.tokenPrefix
    var createdAt by PersonalAccessTokenTable.createdAt
    var expiresAt by PersonalAccessTokenTable.expiresAt
    var lastUsedAt by PersonalAccessTokenTable.lastUsedAt

    fun toDomain() = PersonalAccessToken(
        id = id.value,
        userId = user.id.value,
        name = name,
        tokenHash = tokenHash,
        tokenPrefix = tokenPrefix,
        createdAt = createdAt.toKotlinLocalDateTime(),
        expiresAt = expiresAt?.toKotlinLocalDateTime(),
        lastUsedAt = lastUsedAt?.toKotlinLocalDateTime()
    )

    companion object : IntEntityClass<PersonalAccessTokenEntity>(PersonalAccessTokenTable)
}
