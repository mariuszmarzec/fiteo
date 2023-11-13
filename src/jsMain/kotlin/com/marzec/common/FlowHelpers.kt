package com.marzec.common

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import react.useEffect
import react.useState

@ExperimentalCoroutinesApi
fun <T> useStateFlow(flow: Flow<T>, default: T): T {
    val (state, setState) = useState(default)

    useEffect(Unit) {
        val job = flow.onEach {
            setState(it)
        }.launchIn(GlobalScope)
        cleanup { job.cancel() }
    }

    return state
}
