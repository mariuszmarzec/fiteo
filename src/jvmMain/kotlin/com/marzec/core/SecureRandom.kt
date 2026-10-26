package com.marzec.core

import java.security.SecureRandom as JavaSecureRandom

actual class SecureRandom actual constructor() {
    private val random = JavaSecureRandom()

    actual fun nextBytes(size: Int): ByteArray = ByteArray(size).also { random.nextBytes(it) }
}