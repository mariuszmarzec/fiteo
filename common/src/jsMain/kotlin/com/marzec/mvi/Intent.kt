package com.marzec.mvi

data class Intent<State>(
    val onTrigger: (suspend () -> Any?)?,
    val reducer: suspend (Any, Any?, State) -> State,
    val sideEffect: ((Any?, State) -> Unit)?
)

inline fun <STATE, reified ACTION, reified ACTION_RESULT> intent(
    noinline onTrigger: (suspend () -> ACTION_RESULT?)? = null,
    crossinline reducer: suspend (ACTION, ACTION_RESULT?, STATE) -> STATE,
    noinline sideEffect: ((ACTION_RESULT?, STATE) -> Unit)? = null
) = Intent(
    onTrigger = { onTrigger?.invoke() },
    reducer = { action: Any, actionResult: Any?, currentState: STATE ->
        val typedActionResult = actionResult as? ACTION_RESULT
        reducer(action as ACTION, typedActionResult, currentState)
    },
    sideEffect = { actionResult: Any?, currentState: STATE ->
        val typedActionResult = actionResult as? ACTION_RESULT
        sideEffect?.invoke(typedActionResult, currentState)
    }
)