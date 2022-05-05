package com.marzec.todo.extensions

import com.marzec.todo.model.CreateTask
import com.marzec.todo.model.Task

fun Task.toCreateTask() = CreateTask(
    description = description,
    parentTaskId = parentTaskId,
    priority = priority,
    highestPriorityAsDefault = false,
    scheduler = scheduler
)
