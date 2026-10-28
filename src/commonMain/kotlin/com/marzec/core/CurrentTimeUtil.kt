package com.marzec.core

import kotlinx.datetime.LocalDateTime

expect fun currentTime(): LocalDateTime

expect fun currentMillis(): Long