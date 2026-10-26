package com.marzec.core

/**
 * Computes the lowercase hexadecimal SHA-256 digest of [input].
 *
 * Used to derive the Personal Access Token hash that is persisted in place of
 * the raw secret.
 */
expect fun sha256Hex(input: ByteArray): String