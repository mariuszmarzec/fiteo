package com.marzec.todo.dto

import kotlinx.serialization.Serializable

@Serializable
data class TaskDto(
        val id: Int,
        val description: String,
        val addedTime: String,
        val modifiedTime: String,
        val parentTaskId: Int?,
        val subTasks: List<TaskDto>,
        val isToDo: Boolean,
        val priority: Int
)

@Serializable
data class ToDoListDto(
        val id: Int,
        val title: String,
        val tasks: List<TaskDto>
)

@Serializable
data class CreateTodoListDto(
        val title: String
)