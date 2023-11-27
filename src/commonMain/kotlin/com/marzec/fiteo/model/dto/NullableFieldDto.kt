package com.marzec.fiteo.model.dto

import com.marzec.fiteo.model.domain.NullableField
import kotlinx.serialization.Serializable

@Serializable
data class NullableFieldDto<T>(val value: T?)

fun <T> NullableFieldDto<T>.toDomain() = NullableField(value)

fun <T, R> NullableFieldDto<T>.toDomain(valueMapper: (T?) -> R?): NullableField<R> = NullableField(valueMapper(value))
