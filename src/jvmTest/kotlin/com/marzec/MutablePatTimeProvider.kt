package com.marzec

import com.marzec.core.PatTimeProvider
import kotlinx.datetime.LocalDateTime

internal class MutablePatTimeProvider(
    private var now: LocalDateTime = LocalDateTime(2021, 1, 1, 0, 0)
) : PatTimeProvider {
    override fun currentTime(): LocalDateTime = now

    fun setOtherTime(day: Int, month: Int, year: Int) =
        setOtherTime(day, month, year, 0, 0)

    fun setOtherTime(day: Int, month: Int, year: Int, hour: Int, minute: Int) {
        now = LocalDateTime(year, month, day, hour, minute)
    }
}
