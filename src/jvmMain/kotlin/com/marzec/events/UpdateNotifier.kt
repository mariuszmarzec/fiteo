package com.marzec.events

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

class EventBus {

    private val _events = MutableSharedFlow<Event>()

    val events: SharedFlow<Event>
        get() = _events

    suspend fun send(event: Event) {
        _events.emit(event)
    }
}

sealed class Event {
    data class UpdateEvent(val userId: Int): Event()
}
