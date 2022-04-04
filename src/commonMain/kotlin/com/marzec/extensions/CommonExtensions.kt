package com.marzec.extensions

inline fun <Class, Return> Class.replace(replaceFunction: (Class) -> Return) = replaceFunction(this)

inline fun <Class, Return> Class.replaceOrNull(replaceFunction: (Class?) -> Return) = if (this != null) {
    replaceFunction(this)
} else {
    null
}

@Suppress("FunctionOnlyReturningConstant")
fun emptyString() = ""

fun <T: Any> T.listOf() = listOf(this)

fun <T> T?.ifNull(defaultValue: T) = this ?: defaultValue