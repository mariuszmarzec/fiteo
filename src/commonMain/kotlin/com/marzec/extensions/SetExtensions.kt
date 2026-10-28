package com.marzec.extensions

fun <T> Set<T>.flip(value: T): Set<T> = toMutableSet().apply {
    if (value in this) {
        remove(value)
    } else {
        add(value)
    }
}
