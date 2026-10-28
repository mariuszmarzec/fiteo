package com.marzec.fiteo

import com.marzec.Api

object PatApiPath {

    private const val CURRENT_API_VERSION = "1"
    private const val API = "api"
    private const val APPLICATION_NAME = "pat"
    private const val API_ROOT = "/$APPLICATION_NAME/$API/$CURRENT_API_VERSION"

    const val TOKENS = "$API_ROOT/tokens"
    const val TOKEN_BY_ID = "$TOKENS/{${Api.Args.ARG_ID}}"
}
