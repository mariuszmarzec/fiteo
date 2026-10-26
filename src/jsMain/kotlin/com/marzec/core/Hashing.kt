package com.marzec.core

/**
 * JS stub. The Personal Access Token service is wired and exercised only on the
 * JVM (see Di.kt), so this target does not need a real SHA-256 implementation.
 * Calling it here is a programming error and fails loudly rather than producing
 * a wrong hash.
 */
actual fun sha256Hex(input: ByteArray): String =
    throw NotImplementedError("SHA-256 is not implemented for the JS target; use the JVM target")