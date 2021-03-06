package com.marzec.core

import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

object CurrentTimeUtil {

    var clock = Clock.systemDefaultZone()

    fun setOtherTime(day: Int, month: Int, year: Int) {
        val instant = LocalDate.of(year, month, day).atStartOfDay(ZoneId.systemDefault()).toInstant()
        clock = Clock.fixed(instant, ZoneId.systemDefault())
    }
}

fun currentTime(): LocalDateTime = LocalDateTime.now(CurrentTimeUtil.clock)
