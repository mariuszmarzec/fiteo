package com.marzec.todo.dto

data class TaskDto(
        val id: Int,
        val description: String,
        val addedTime: String,
        val modifiedTime: String,
        val parentTask: TaskDto?,
        val subTasks: List<TaskDto>,
        val isToDo: Boolean,
        val priority: Int
)

data class ToDoListDto(
        val id: Int,
        val title: String,
        val tasks: List<TaskDto>
)