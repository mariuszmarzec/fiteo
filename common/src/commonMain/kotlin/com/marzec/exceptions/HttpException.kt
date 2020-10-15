package com.marzec.exceptions

class HttpException(message: String?, val httpStatus: Int) : Exception(message)