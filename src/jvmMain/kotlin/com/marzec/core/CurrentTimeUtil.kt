package com.marzec.core

import kotlinx.datetime.toKotlinLocalDateTime
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.*
import kotlinx.datetime.LocalDateTime as KotlinLocalDateTime

object CurrentTimeUtil {

    internal var clock = Clock.systemDefaultZone()

    fun init(timeZone: TimeZone = TimeZone.getTimeZone("UTC")) {
        TimeZone.setDefault(timeZone)
        clock = Clock.systemDefaultZone()
    }

    fun setOtherTime(day: Int, month: Int, year: Int) {
        val instant = LocalDate.of(year, month, day).atStartOfDay(ZoneId.systemDefault()).toInstant()
        clock = Clock.fixed(instant, ZoneId.systemDefault())
    }

    fun setOtherTime(day: Int, month: Int, year: Int, hour: Int, minute: Int) {
        val instant = LocalDateTime.of(year, month, day, hour, minute).toInstant(ZoneOffset.ofHours(4))
        clock = Clock.fixed(instant, ZoneId.systemDefault())
    }
}

actual fun currentTime(): KotlinLocalDateTime = LocalDateTime.now(CurrentTimeUtil.clock).toKotlinLocalDateTime()

actual fun currentMillis(): Long = CurrentTimeUtil.clock.millis()
