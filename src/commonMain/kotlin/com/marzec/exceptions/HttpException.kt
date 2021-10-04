package com.marzec.exceptions

class HttpException(message: String?, val httpStatus: Int) : Exception(message)

object HttpStatus {
    const val BAD_REQUEST = 400
    const val NOT_FOUND = 404
    const val INTERNAL_SERVER_ERROR = 500
}
