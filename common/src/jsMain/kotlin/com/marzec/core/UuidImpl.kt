package com.marzec.core

actual class UuidImpl : Uuid {
    actual override fun create(): String {
        return ""
    }
}