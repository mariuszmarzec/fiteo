package com.marzec.extensions

import com.marzec.fiteo.model.domain.NullableField
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.serializer
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty

inline fun <reified T> Map<String, JsonElement?>.getByProperty(property: KProperty<T?>): T? =
    getByProperty<T, T>(property) { it }

inline fun <T, reified V> Map<String, JsonElement?>.getByProperty(property: KProperty<T?>, mapper: (V) -> T): T? =
    get(property.name)?.let {
        Json.decodeFromJsonElement(serializer<V>(), it)
    }?.let { mapper(it) }

inline fun <reified T> Map<String, JsonElement?>.getNullableByProperty(property: KProperty<NullableField<T>?>): NullableField<T>? =
    getNullableByProperty<T, T>(property) { it }


inline fun <T, reified V> Map<String, JsonElement?>.getNullableByProperty(
    property: KProperty<NullableField<T>?>,
    mapper: (V) -> T
): NullableField<T>? =
    if (containsKey(property.name)) {
        (get(property.name) as? JsonObject)?.getValue(NullableField<Any>::value.name)?.let { jsonElement ->
            println(jsonElement)
            Json.decodeFromJsonElement(serializer<V>(), jsonElement)
                ?.let { mapper(it) }
                .let { NullableField(it) }
        }
    } else {
        null
    }

fun <T> update(toUpdate: KMutableProperty0<T>, updatedValue: T?) {
    updatedValue?.let { toUpdate.set(it) }
}

fun <T> updateNullable(toUpdate: KMutableProperty0<T?>, updatedValue: NullableField<T>?) {
    updatedValue?.let { toUpdate.set(it.value) }
}

fun <T, V> updateByNullable(toUpdate: KMutableProperty0<T>, updatedValue: NullableField<V>?, transform:(V?) -> T) {
    updatedValue?.let { toUpdate.set(transform(it.value)) }
}
