package com.marzec.core

/**
 * Platform-specific source of cryptographically strong random bytes.
 *
 * The Personal Access Token service uses this to generate the raw secret that
 * is never persisted. Only the SHA-256 hash of the secret is stored.
 */
expect class SecureRandom {
    constructor()

    fun nextBytes(size: Int): ByteArray
}