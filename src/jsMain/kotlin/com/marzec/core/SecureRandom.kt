package com.marzec.core

import kotlin.random.Random

actual class SecureRandom actual constructor() {
    actual fun nextBytes(size: Int): ByteArray = ByteArray(size).also { Random.Default.nextBytes(it) }
}