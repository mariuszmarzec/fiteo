package com.marzec.common

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import react.useEffectWithCleanup
import react.useState

@ExperimentalCoroutinesApi
fun <T> useStateFlow(flow: Flow<T>, default: T): T {
    val (state, setState) = useState(default)

    useEffectWithCleanup(listOf()) {
        val job = flow.onEach {
            setState(it)
        }.launchIn(GlobalScope)
        return@useEffectWithCleanup { job.cancel() }
    }

    return state
}
