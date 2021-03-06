package com.marzec.cheatday

object ApiPath {

    const val CURRENT_API_VERSION = "1"

    const val API = "api"

    const val ARG_ID = "id"

    const val APPLICATION_NAME = "cheat"

    const val API_ROOT = "/$APPLICATION_NAME/$API/$CURRENT_API_VERSION/"

    const val WEIGHTS = "$API_ROOT/weights"
    const val WEIGHT = "$API_ROOT/weight"
    const val UPDATE_WEIGHT = "$API_ROOT/weight/{$ARG_ID}"
    const val REMOVE_WEIGHT = "$API_ROOT/weight/{$ARG_ID}"

}