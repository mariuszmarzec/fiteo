package com.marzec.mvi

data class Intent<State>(
    val onTrigger: (suspend () -> Any?)?,
    val reducer: suspend (Any, Any?, State) -> State,
    val sideEffect: ((Any?, State) -> Unit)?
)