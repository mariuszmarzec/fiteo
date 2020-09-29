package com.marzec.model.domain

sealed class Request<T> {
    data class Success<T>(val data: T) : Request<T>()
    data class Error<T>(val reason: String, val httpStatus: Int = 400) : Request<T>()
}