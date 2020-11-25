package com.marzec

import io.ktor.client.HttpClient
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer

val jsonClient = HttpClient {
    install(JsonFeature) { serializer = KotlinxSerializer() }
}
