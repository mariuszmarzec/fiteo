package com.marzec.todo.extensions

import com.marzec.todo.model.Task

fun List<Task>.sortTasks() = sortedWith(compareByDescending(Task::isToDo).thenByDescending { it.priority }.thenBy { it.modifiedTime })
