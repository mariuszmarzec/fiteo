package com.marzec.todo.dto

import com.marzec.todo.model.SchedulerDto
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
    val priority: Int,
    val scheduler: SchedulerDto?
)
