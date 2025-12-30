package com.marzec.database

import com.marzec.core.currentTime
import kotlinx.datetime.toJavaLocalDateTime
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object FcmTokenTable : IntIdTable("fcm_tokens") {
    val userId = reference("user_id", UserTable)
    val fcmToken = varchar("fcm_token", 255).uniqueIndex()
    val platform = varchar("platform", 50).nullable()
    val updatedAt = datetime("updated_at").default(currentTime().toJavaLocalDateTime())
}

class FcmTokenEntity(id: EntityID<Int>) : IntEntity(id) {
    var user by UserEntity referencedOn FcmTokenTable.userId
    var fcmToken by FcmTokenTable.fcmToken
    var platform by FcmTokenTable.platform
    var updatedAt by FcmTokenTable.updatedAt

    companion object : IntEntityClass<FcmTokenEntity>(FcmTokenTable)
}
