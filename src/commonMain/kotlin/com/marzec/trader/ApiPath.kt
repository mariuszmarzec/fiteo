package com.marzec.trader

import com.marzec.Api

object ApiPath {

    private const val CURRENT_API_VERSION = "1"

    private const val API = "api"
    
    private const val APPLICATION_NAME = "trader"

    private const val API_ROOT = "/$APPLICATION_NAME/$API/$CURRENT_API_VERSION"

    const val UPDATE_PAPERS = "${API_ROOT}/papers/{${Api.Args.ARG_ID}}"
    const val DELETE_PAPERS = "${API_ROOT}/papers/{${Api.Args.ARG_ID}}"

    const val PAPERS = "${API_ROOT}/papers"
    const val ADD_PAPER = "${API_ROOT}/papers"

    const val PAPER_TAGS = "$PAPERS/tags"
    const val ADD_PAPER_TAG = "$PAPERS/tags"
    const val UPDATE_PAPER_TAG = "$PAPERS/tags/{${Api.Args.ARG_ID}}"
    const val DELETE_PAPER_TAG = "$PAPERS/tags/{${Api.Args.ARG_ID}}"

    const val UPDATE_TRANSACTIONS = "${API_ROOT}/transactions/{${Api.Args.ARG_ID}}"
    const val DELETE_TRANSACTIONS = "${API_ROOT}/transactions/{${Api.Args.ARG_ID}}"

    const val TRANSACTIONS = "${API_ROOT}/transactions"
    const val ADD_TRANSACTIONS = "${API_ROOT}/transactions"
}
