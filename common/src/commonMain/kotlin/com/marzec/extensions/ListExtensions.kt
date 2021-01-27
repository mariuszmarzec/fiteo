package com.marzec.extensions

fun <T> List<T>.replaceIf(condition: (T) -> Boolean, replace: (T) -> T) = map { item ->
    if (condition(item)) {
        replace(item)
    } else {
        item
    }
}