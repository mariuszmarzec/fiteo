package com.marzec.sessions

import com.marzec.fiteo.repositories.CachedSessionsRepository
import io.ktor.server.sessions.SessionStorage
import io.ktor.util.cio.toByteArray
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.writer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import com.marzec.fiteo.model.domain.CachedSession

class DatabaseSessionStorage(
        private val repository: CachedSessionsRepository
) : SessionStorage {
    override suspend fun invalidate(id: String) {
        repository.removeSession(id)
    }
// TODO CACHED SESSION, REMOVE BLOB
    override suspend fun read(id: String): String =
        String(repository.getSession(id)?.session ?: ByteArray(0)) ?: throw NoSuchElementException("Session $id not found")

    override suspend fun write(id: String, value: String) {
        repository.createSession(CachedSession(id, value.toByteArray(Charsets.UTF_8)))
    }
}
