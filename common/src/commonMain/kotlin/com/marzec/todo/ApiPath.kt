package com.marzec.todo

object ApiPath {

    const val CURRENT_API_VERSION = "1"

    const val API = "api"

    const val ARG_ID = "id"

    const val APPLICATION_NAME = "todo"

    const val API_ROOT = "/$APPLICATION_NAME/$API/$CURRENT_API_VERSION/"

    const val TODO_LISTS = "$API_ROOT/lists"
    const val TODO_LIST = "$API_ROOT/list"
    const val DELETE_TODO_LIST = "$API_ROOT/list/{$ARG_ID}"
}