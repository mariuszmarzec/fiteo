package com.marzec.core

import java.security.MessageDigest

fun sha256Hex(input: ByteArray): String {
    val digest = MessageDigest.getInstance("SHA-256")
    return digest.digest(input).joinToString("") { "%02x".format(it.toInt() and 0xFF) }
}