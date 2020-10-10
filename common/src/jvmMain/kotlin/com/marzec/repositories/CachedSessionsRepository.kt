package com.marzec.repositories

import com.marzec.database.CachedSessionEntity
import com.marzec.database.CachedSessionTable
import com.marzec.database.dbCall
import com.marzec.database.toDomain
import com.marzec.model.domain.CachedSession
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.statements.api.ExposedBlob

class CachedSessionsRepositoryImpl : CachedSessionsRepository {

    override fun createSession(session: CachedSession) {
        dbCall {
            CachedSessionEntity.new(session.id) {
                this.session = ExposedBlob(session.session)
            }
        }
    }

    override fun getSession(id: String): CachedSession? {
        return dbCall {
            CachedSessionEntity.findById(id)?.toDomain()
        }
    }

    override fun removeSession(id: String) {
        dbCall {
            CachedSessionTable.deleteWhere { CachedSessionTable.id eq id }
        }
    }

}