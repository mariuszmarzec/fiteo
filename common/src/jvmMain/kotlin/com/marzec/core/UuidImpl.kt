package com.marzec.core

import java.util.UUID

actual class UuidImpl : Uuid {
    actual override fun create(): String {
        return UUID.randomUUID().toString()
    }
}