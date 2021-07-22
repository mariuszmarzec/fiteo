package com.marzec.extensions

import com.marzec.Api
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDateTime
import java.time.format.DateTimeFormatter

actual fun LocalDateTime.formatDate(): String = toJavaLocalDateTime().format(DateTimeFormatter.ofPattern(Api.DATE_FORMAT))
