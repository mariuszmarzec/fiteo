package com.marzec.todo

import com.marzec.Api

object ApiPath {

    private const val CURRENT_API_VERSION = "1"

    private const val API = "api"
    
    private const val APPLICATION_NAME = "todo"

    private const val API_ROOT = "/$APPLICATION_NAME/$API/$CURRENT_API_VERSION/"

    const val TODO_LISTS = "$API_ROOT/lists"
    const val TODO_LIST = "$API_ROOT/list"
    const val DELETE_TODO_LIST = "$API_ROOT/list/{${Api.Args.ARG_ID}}"

    const val ADD_TASK = "$API_ROOT/list/{${Api.Args.ARG_ID}}/tasks"
    const val UPDATE_TASK = "$API_ROOT/tasks/{${Api.Args.ARG_ID}}"
    const val DELETE_TASK = "$API_ROOT/tasks/{${Api.Args.ARG_ID}}"
}
