package com.marzec.common

sealed class Resource<T> {

    data class Content<T>(val data: T) : Resource<T>()
    data class Error<T>(val message: String) : Resource<T>()
}