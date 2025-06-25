package com.marzec

import com.marzec.Api.Headers
import com.marzec.di.NAME_SESSION_EXPIRATION_TIME
import com.marzec.events.Event
import com.marzec.events.EventBus
import io.ktor.client.plugins.sse.*
import io.ktor.client.request.*
import io.ktor.server.testing.*
import io.ktor.sse.*
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Test
import org.koin.core.context.GlobalContext
import org.koin.core.qualifier.named
import kotlin.test.assertEquals

class SseTest {

    @Test
    fun sse() = runTest {
        val eventBus = EventBus()
        withMockTestApplication(
            withDbClear = true,
            mockConfiguration = {
                defaultMockConfiguration()
                single { eventBus }
            }
        ) {
            val events = mutableListOf<ServerSentEvent>()
            authToken = registerAndLogin()
            backgroundScope.launch {
                client.config {
                    install(SSE)
                }.sse(
                    "/sse",
                    request = {
                        header(Headers.AUTHORIZATION, authToken)
                    }
                ) {
                    incoming.collect {
                        events.add(it)
                    }
                }
            }
            eventBus.send(Event.UpdateEvent(1))

            assertEquals(listOf(ServerSentEvent("UPDATE")), events)
        }
    }

    @After
    fun tearDown() {
        GlobalContext.stopKoin()
    }
}