package com.marzec.mvi

import kotlin.reflect.KClass
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
class Store<Type, Action : Any>(defaultState: State<Type>) {

    private val scope = MainScope()

    var intents = mapOf<KClass<out Action>, Intent<State<Type>>>()

    private val _state = MutableStateFlow(defaultState)

    val state: Flow<State<Type>>
        get() = _state

    fun sendAction(action: Action) {
        scope.launch {
            val intent = intents[action::class]
            requireNotNull(intent)

            val result = intent.onTrigger?.invoke()

            val newState = intent.reducer(action, result, _state.value)

            _state.value = newState

            intent.sideEffect?.invoke(result, _state.value)
        }
    }
}
