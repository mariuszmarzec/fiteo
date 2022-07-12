package com.marzec

import io.ktor.client.HttpClient
import io.ktor.client.plugins.kotlinx.serializer.*

val jsonClient = HttpClient {
    install(JsonFeature) { serializer = KotlinxSerializer() }
}
