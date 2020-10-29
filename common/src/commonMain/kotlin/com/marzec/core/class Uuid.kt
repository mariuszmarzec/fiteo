package com.marzec.core

interface Uuid {
    fun create(): String
}

expect class UuidImpl : Uuid {
    override fun create(): String
}