package com.marzec.core

import kotlinx.datetime.toKotlinLocalDateTime
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*
import kotlinx.datetime.LocalDateTime as KotlinLocalDateTime

object CurrentTimeUtil {

    internal var clock = Clock.systemDefaultZone()

    fun init(timeZone: TimeZone = TimeZone.getTimeZone("UTC")) {
        TimeZone.setDefault(timeZone)
        clock = Clock.system(timeZone.toZoneId())
    }

    fun setOtherTime(day: Int, month: Int, year: Int) {
        val instant = LocalDate.of(year, month, day).atStartOfDay(ZoneId.systemDefault()).toInstant()
        clock = Clock.fixed(instant, ZoneId.systemDefault())
    }

    fun setOtherTime(day: Int, month: Int, year: Int, hour: Int, minute: Int) {
        val instant = ZonedDateTime.of(year, month, day, hour, minute, 0, 0, ZoneId.systemDefault()).toInstant()
        clock = Clock.fixed(instant, ZoneId.systemDefault())
    }
}

actual fun currentTime(): KotlinLocalDateTime = ZonedDateTime.now(CurrentTimeUtil.clock).toLocalDateTime().toKotlinLocalDateTime()

actual fun currentMillis(): Long = CurrentTimeUtil.clock.millis()
