package com.marzec

import com.marzec.Api.Headers
import com.marzec.di.MainModule
import com.marzec.di.diModules
import com.marzec.events.Event
import com.marzec.events.EventBus
import com.marzec.fiteo.ApiPath
import com.marzec.fiteo.model.dto.LoginRequestDto
import io.ktor.client.*
import io.ktor.client.plugins.sse.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.sse.*
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Test
import org.koin.dsl.module
import kotlin.test.assertEquals


class SseTest {

    @Test
    fun sse() = runBlocking {
        val eventBus = EventBus()
        if (true) {
            setupDb()
        }
        diModules = MainModule.plus(module {
            defaultMockConfiguration()
            single { eventBus }
        })
        val server = embeddedServer(Netty, port = 8081, module = io.ktor.server.application.Application::module).start()
        val client = HttpClient {
            install(SSE)
        }
        client.apply {

            val events = mutableListOf<ServerSentEvent>()

            val dto = LoginRequestDto(email = "mariusz.marzec00@gmail.com", password = "password")

            val authToken = client.request("http://localhost:8081" + ApiPath.LOGIN) {
                this.method = HttpMethod.Post
                setBodyJson(dto)
            }.headers[Headers.AUTHORIZATION]!!
            runBlocking {
                val job = launch {
                    client.config {
                        install(SSE)
                    }.sse(
                        "http://localhost:8081/sse",
                        request = {
                            header(Headers.AUTHORIZATION, authToken)
                        }
                    ) {
                        incoming.collect { event ->
                            events.add(event)
                        }

                    }
                }
                delay(200)
                eventBus.send(Event.UpdateEvent(1))

                withTimeout(2000) {
                    while (events.isEmpty()) {
                        delay(100)
                    }
                }

                job.cancelAndJoin()
            }
            assertEquals(listOf(ServerSentEvent("UPDATE")), events)
        }
        server.stop()
    }
}