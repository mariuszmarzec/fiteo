package com.marzec.fiteo

object PatApiPath {

    private const val CURRENT_API_VERSION = "1"
    private const val API = "api"
    private const val APPLICATION_NAME = "pat"
    private const val API_ROOT = "/$APPLICATION_NAME/$API/$CURRENT_API_VERSION"

    const val TOKENS = "$API_ROOT/tokens"
}
