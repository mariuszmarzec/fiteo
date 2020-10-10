package com.marzec.model.domain

data class CachedSession(val id: String, val session: ByteArray) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as CachedSession

        if (id != other.id) return false
        if (!session.contentEquals(other.session)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + session.contentHashCode()
        return result
    }
}