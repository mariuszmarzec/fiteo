package com.marzec.sessions

import com.marzec.fiteo.model.domain.CachedSession
import com.marzec.fiteo.repositories.CachedSessionsRepository
import io.ktor.sessions.SessionStorage
import io.ktor.util.cio.toByteArray
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.writer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope

class DatabaseSessionStorage(
        private val repository: CachedSessionsRepository
) : SessionStorage {
    override suspend fun invalidate(id: String) {
        repository.removeSession(id)
    }

    override suspend fun <R> read(id: String, consumer: suspend (ByteReadChannel) -> R): R {
        return repository.getSession(id)?.session?.let { data -> consumer(ByteReadChannel(data)) }
                ?: throw NoSuchElementException("Session $id not found")
    }

    override suspend fun write(id: String, provider: suspend (ByteWriteChannel) -> Unit) {
        coroutineScope {
            val channel = writer(Dispatchers.Unconfined, autoFlush = true) {
                provider(channel)
            }.channel

            repository.createSession(CachedSession(id, channel.toByteArray()))
        }
    }
}