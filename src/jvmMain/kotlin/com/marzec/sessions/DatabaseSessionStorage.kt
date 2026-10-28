package com.marzec.sessions

import com.marzec.fiteo.repositories.CachedSessionsRepository
import io.ktor.server.sessions.SessionStorage
import com.marzec.fiteo.model.domain.CachedSession

class DatabaseSessionStorage(
        private val repository: CachedSessionsRepository
) : SessionStorage {
    override suspend fun invalidate(id: String) {
        repository.removeSession(id)
    }

    override suspend fun read(id: String): String =
        repository.getSession(id)?.session ?: throw NoSuchElementException("Session $id not found")

    override suspend fun write(id: String, value: String) {
        repository.createSession(CachedSession(id, value))
    }
}
