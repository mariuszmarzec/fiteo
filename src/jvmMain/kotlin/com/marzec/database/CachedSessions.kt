package com.marzec.database

import com.marzec.fiteo.model.domain.CachedSession
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.statements.api.ExposedBlob

object CachedSessionTable : IdTable<String>("cached_sessions") {
    private const val ID_LENGTH = 1000
    override val id: Column<EntityID<String>> = varchar("id", ID_LENGTH).entityId()
    val session: Column<ExposedBlob> = blob("session")
}

class CachedSessionEntity(id: EntityID<String>): Entity<String>(id) {
    companion object : EntityClass<String, CachedSessionEntity>(CachedSessionTable)
    var session by CachedSessionTable.session
}

fun CachedSessionEntity.toDomain(): CachedSession = CachedSession(id.value, session.bytes)
