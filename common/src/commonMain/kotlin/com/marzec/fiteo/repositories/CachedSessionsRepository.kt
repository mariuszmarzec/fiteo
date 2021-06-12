package com.marzec.repositories

import com.marzec.model.domain.CachedSession

interface CachedSessionsRepository {

    fun createSession(session: CachedSession)

    fun getSession(id: String): CachedSession?

    fun removeSession(id: String)
}