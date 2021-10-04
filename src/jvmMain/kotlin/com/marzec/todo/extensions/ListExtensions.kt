package com.marzec.todo.extensions

import com.marzec.todo.model.Task

fun List<Task>.sortTasks() = sortedWith(compareByDescending(Task::priority).thenBy { it.modifiedTime })
