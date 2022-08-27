package com.marzec.todo

import com.marzec.Api

object ApiPath {

    private const val CURRENT_API_VERSION = "1"

    private const val API = "api"
    
    private const val APPLICATION_NAME = "todo"

    private const val API_ROOT = "/$APPLICATION_NAME/$API/$CURRENT_API_VERSION"

    const val MARK_AS_TO_DO = "$API_ROOT/tasks/mark-as-to-do"
    const val UPDATE_TASK = "$API_ROOT/tasks/{${Api.Args.ARG_ID}}"
    const val DELETE_TASK = "$API_ROOT/tasks/{${Api.Args.ARG_ID}}"
    const val DELETE_TASK_WITH_SUBTASKS = "$API_ROOT/tasks/{${Api.Args.ARG_ID}}/removeWithSubtasks"

    const val TASKS = "$API_ROOT/tasks"
    const val COPY_TASK = "$API_ROOT/tasks/{${Api.Args.ARG_ID}}/copy"
    const val ADD_TASK = "$API_ROOT/tasks"
}
