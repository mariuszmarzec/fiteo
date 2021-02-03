package com.marzec.mvi

sealed class State<T> {

    data class Data<T>(val data: T) : State<T>()
    data class Loading<T>(val data: T? = null) : State<T>()
    data class Error<T>(val message: String) : State<T>()
}