package com.marzec.fiteo.repositories

import com.marzec.fiteo.model.domain.CachedSession

interface CachedSessionsRepository {

    fun createSession(session: CachedSession)

    fun getSession(id: String): CachedSession?

    fun removeSession(id: String)

    suspend fun clearOldSessions()
}
