package com.marzec

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.*
import kotlin.js.json

val jsonClient = HttpClient {
    install(ContentNegotiation) {
        json()
    }
}
