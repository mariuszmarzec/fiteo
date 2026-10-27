package com.marzec.core

import java.time.Clock
import java.time.ZoneOffset
import java.time.LocalDateTime as JavaLocalDateTime
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime

/**
 * Provides PAT timestamps as UTC wall-clock values.
 *
 * SQL DATETIME values have no zone, so PAT timestamps use UTC by convention
 * for creation, expiration checks, and last-used updates.
 */
interface PatTimeProvider {
    fun currentTime(): LocalDateTime
}

class UtcPatTimeProvider(private val clock: Clock = Clock.systemUTC()) : PatTimeProvider {
    override fun currentTime(): LocalDateTime =
        JavaLocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC).toKotlinLocalDateTime()
}
