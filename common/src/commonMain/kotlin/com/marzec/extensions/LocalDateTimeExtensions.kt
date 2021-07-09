package com.marzec.extensions

import kotlinx.datetime.LocalDateTime

fun LocalDateTime.formatDate(): String = LocalDateTimeExtensions.format(this)

// TODO broken actual / expect
object LocalDateTimeExtensions {

    var formatter: (LocalDateTime) -> String = { date -> date.toString() }

    fun format(date: LocalDateTime): String = formatter(date)
}