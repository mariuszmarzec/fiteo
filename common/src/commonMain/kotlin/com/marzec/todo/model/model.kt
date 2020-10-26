package com.marzec.todo.model

data class Task(
        val id: Int,
        val description: String,
        val addedTime: Long,
        val modifiedTime: Long,
        val parentTask: Task?,
        val list: ToDoList,
        val subTasks: List<Task>,
        val isToDo: Boolean,
        val priority: Int
)

data class ToDoList(
        val id: Int,
        val title: String,
        val tasks: List<Task>
)