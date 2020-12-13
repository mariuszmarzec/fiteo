package com.marzec.todo.model

import com.marzec.todo.dto.TaskDto
import com.marzec.todo.dto.ToDoListDto
import kotlinx.datetime.LocalDateTime

data class Task(
        val id: Int,
        val description: String,
        val addedTime: LocalDateTime,
        val modifiedTime: LocalDateTime,
        val parentTask: Task?,
        val subTasks: List<Task>,
        val isToDo: Boolean,
        val priority: Int
)

data class ToDoList(
        val id: Int,
        val title: String,
        val tasks: List<Task>
)

fun ToDoList.toDto() = ToDoListDto(
        id = id,
        title = title,
        tasks = tasks.map { it.toDto() }
)

fun Task.toDto(): TaskDto {
    val parentTaskDto = parentTask?.toDto()
    return TaskDto(
            id = id,
            description = description,
            addedTime = LocalDateTime.toString(),
            modifiedTime = modifiedTime.toString(),
            parentTask = parentTaskDto,
            subTasks = subTasks.map { it.toDto() },
            isToDo = isToDo,
            priority = priority
    )
}