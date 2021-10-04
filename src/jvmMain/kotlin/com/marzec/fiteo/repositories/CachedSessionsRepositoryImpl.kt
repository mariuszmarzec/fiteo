package com.marzec.fiteo.repositories

import com.marzec.core.currentMillis
import com.marzec.database.CachedSessionEntity
import com.marzec.database.CachedSessionTable
import com.marzec.database.dbCall
import com.marzec.database.toDomain
import com.marzec.fiteo.model.domain.CachedSession
import com.marzec.fiteo.model.domain.TestUserSession
import com.marzec.fiteo.model.domain.UserSession
import io.ktor.sessions.defaultSessionSerializer
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.statements.api.ExposedBlob

class CachedSessionsRepositoryImpl(
    private val database: Database,
    private val sessionExpirationTime: Long
) : CachedSessionsRepository {

    override fun createSession(session: CachedSession) {
        database.dbCall {
            if (CachedSessionEntity.findById(session.id) == null) {
                CachedSessionEntity.new(session.id) {
                    this.session = ExposedBlob(session.session)
                }
            }
        }
    }

    override fun getSession(id: String): CachedSession? {
        return database.dbCall {
            CachedSessionEntity.findById(id)?.toDomain()
        }
    }

    override fun removeSession(id: String) {
        database.dbCall {
            CachedSessionTable.deleteWhere { CachedSessionTable.id eq id }
        }
    }

    override suspend fun clearOldSessions() = database.dbCall {
        CachedSessionEntity.all().toList().distinctBy { it.id }.forEach { entity ->
            CoroutineScope(Dispatchers.Unconfined).launch {
                val session = readSession(entity).orEmpty()
                val userSession = session.deserializeSession<UserSession>()
                userSession?.let {
                    if (isSessionExpired(userSession.timestamp)) {
                        entity.delete()
                    }
                } ?: run {
                    val testUserSession = session.deserializeSession<TestUserSession>()
                    testUserSession?.let {
                        if (isSessionExpired(testUserSession.timestamp)) {
                            entity.delete()
                        }
                    }
                }
            }
        }
    }

    private suspend fun readSession(it: CachedSessionEntity) =
        ByteReadChannel(it.session.bytes).readUTF8Line()

    private fun isSessionExpired(timestamp: Long) = currentMillis() - timestamp > sessionExpirationTime
}

private inline fun <reified T : Any> String.deserializeSession(): T? {
    return try {
        defaultSessionSerializer<T>().deserialize(this)
    } catch (ignore: Exception) {
        null
    }
}
