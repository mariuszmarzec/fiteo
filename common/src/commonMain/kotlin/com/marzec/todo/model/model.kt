package com.marzec.todo.model

import com.marzec.Api.Default.HIGHEST_PRIORITY_AS_DEFAULT
import com.marzec.extensions.formatDate
import com.marzec.todo.dto.TaskDto
import com.marzec.todo.dto.ToDoListDto
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

data class Task(
        val id: Int,
        val description: String,
        val addedTime: LocalDateTime,
        val modifiedTime: LocalDateTime,
        val parentTaskId: Int?,
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

fun Task.toDto(): TaskDto = TaskDto(
        id = id,
        description = description,
        addedTime = addedTime.formatDate(),
        modifiedTime = modifiedTime.formatDate(),
        parentTaskId = parentTaskId,
        subTasks = subTasks.map { it.toDto() },
        isToDo = isToDo,
        priority = priority
)

fun TaskDto.toDomain(): Task = Task(
            id = id,
            description = description,
            addedTime = LocalDateTime.parse(addedTime),
            modifiedTime = LocalDateTime.parse(modifiedTime),
            parentTaskId = parentTaskId,
            subTasks = subTasks.map { it.toDomain() },
            isToDo = isToDo,
            priority = priority
    )

data class CreateTask(
        val description: String,
        val parentTaskId: Int?,
        val priority: Int?,
        val highestPriorityAsDefault: Boolean
)

@Serializable
data class CreateTaskDto(
        val description: String,
        val parentTaskId: Int? = null,
        val priority: Int? = null,
        val highestPriorityAsDefault: Boolean? = null
)

fun CreateTaskDto.toDomain() = CreateTask(
    description = description,
    parentTaskId = parentTaskId,
    priority = priority,
    highestPriorityAsDefault ?: HIGHEST_PRIORITY_AS_DEFAULT
)

fun CreateTask.toDto() = CreateTaskDto(
        description = description,
        parentTaskId = parentTaskId,
        priority = priority,
)

data class UpdateTask(
        val description: String,
        val parentTaskId: Int?,
        val priority: Int,
        val isToDo: Boolean
)

@Serializable
data class UpdateTaskDto(
        val description: String,
        val parentTaskId: Int? = null,
        val priority: Int,
        val isToDo: Boolean
)

fun UpdateTaskDto.toDomain() = UpdateTask(
        description = description,
        parentTaskId = parentTaskId,
        priority = priority,
        isToDo = isToDo
)

fun UpdateTask.toDto() = UpdateTaskDto(
        description = description,
        parentTaskId = parentTaskId,
        priority = priority,
        isToDo = isToDo
)