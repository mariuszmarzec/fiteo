package com.marzec.core

import kotlinx.datetime.LocalDateTime

interface TimeProvider {

    fun currentTime(): LocalDateTime
}
