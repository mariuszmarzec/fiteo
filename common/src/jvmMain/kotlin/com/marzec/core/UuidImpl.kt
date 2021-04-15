package com.marzec.core

import java.util.UUID

class UuidImpl : Uuid {
    override fun create(): String {
        return UUID.randomUUID().toString()
    }
}