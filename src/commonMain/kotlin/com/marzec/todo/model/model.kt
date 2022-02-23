package com.marzec.todo.model

import com.marzec.Api.Default.HIGHEST_PRIORITY_AS_DEFAULT
import com.marzec.extensions.formatDate
import com.marzec.todo.dto.TaskDto
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable

data class Task(
    val id: Int,
    val description: String,
    val addedTime: LocalDateTime,
    val modifiedTime: LocalDateTime,
    val parentTaskId: Int?,
    val subTasks: List<Task>,
    val isToDo: Boolean,
    val priority: Int,
    val scheduler: Scheduler?
)

sealed class Scheduler(
    open val hour: Int,
    open val minute: Int,
    open val startDate: LocalDateTime
) {
    data class OneShot(
        override val hour: Int,
        override val minute: Int,
        override val startDate: LocalDateTime
    ) : Scheduler(hour, minute, startDate)

    data class Weekly(
        override val hour: Int,
        override val minute: Int,
        override val startDate: LocalDateTime,
        val daysOfWeek: List<DayOfWeek>,
    ) : Scheduler(hour, minute, startDate)

    data class Monthly(
        override val hour: Int,
        override val minute: Int,
        override val startDate: LocalDateTime,
        val dayOfMonth: Int,
    ) : Scheduler(hour, minute, startDate)
}

@Serializable
data class SchedulerDto(
    val hour: Int,
    val minute: Int,
    val startDate: String,
    val daysOfWeek: List<Int>,
    val dayOfMonth: Int,
    val type: String
)

fun SchedulerDto.toDomain(): Scheduler = when(type) {
    Scheduler.OneShot::class.simpleName -> Scheduler.OneShot(
        hour = hour,
        minute = minute,
        startDate = startDate.toLocalDateTime()
    )
    Scheduler.Weekly::class.simpleName -> Scheduler.Weekly(
        hour = hour,
        minute = minute,
        startDate = startDate.toLocalDateTime(),
        daysOfWeek = daysOfWeek.map { DayOfWeek(it) }
    )
    Scheduler.Monthly::class.simpleName -> Scheduler.Monthly(
        hour = hour,
        minute = minute,
        startDate = startDate.toLocalDateTime(),
        dayOfMonth = dayOfMonth
    )
    else -> throw IllegalArgumentException("Unknown type of scheduler")
}

fun Scheduler.toDto(): SchedulerDto = when (this) {
    is Scheduler.OneShot -> SchedulerDto(
        hour = hour,
        minute = minute,
        startDate = startDate.formatDate(),
        daysOfWeek = emptyList(),
        dayOfMonth = 0,
        type = this::class.simpleName.orEmpty()
    )
    is Scheduler.Weekly -> SchedulerDto(
        hour = hour,
        minute = minute,
        startDate = startDate.formatDate(),
        daysOfWeek = daysOfWeek.map { it.isoDayNumber },
        dayOfMonth = 0,
        type = this::class.simpleName.orEmpty()
    )
    is Scheduler.Monthly -> SchedulerDto(
        hour = hour,
        minute = minute,
        startDate = startDate.formatDate(),
        daysOfWeek = emptyList(),
        dayOfMonth = dayOfMonth,
        type = this::class.simpleName.orEmpty()
    )
}

fun Task.toDto(): TaskDto = TaskDto(
    id = id,
    description = description,
    addedTime = addedTime.formatDate(),
    modifiedTime = modifiedTime.formatDate(),
    parentTaskId = parentTaskId,
    subTasks = subTasks.map { it.toDto() },
    isToDo = isToDo,
    priority = priority,
//    scheduler = scheduler?.toDto()
)

fun TaskDto.toDomain(): Task = Task(
    id = id,
    description = description,
    addedTime = LocalDateTime.parse(addedTime),
    modifiedTime = LocalDateTime.parse(modifiedTime),
    parentTaskId = parentTaskId,
    subTasks = subTasks.map { it.toDomain() },
    isToDo = isToDo,
    priority = priority,
    scheduler = null// scheduler?.toDomain()
)

data class CreateTask(
    val description: String,
    val parentTaskId: Int?,
    val priority: Int?,
    val highestPriorityAsDefault: Boolean,
    val scheduler: Scheduler?
)

@Serializable
data class CreateTaskDto(
    val description: String,
    val parentTaskId: Int? = null,
    val priority: Int? = null,
    val highestPriorityAsDefault: Boolean? = null,
    val scheduler: SchedulerDto?
)

fun CreateTaskDto.toDomain() = CreateTask(
    description = description,
    parentTaskId = parentTaskId,
    priority = priority,
    highestPriorityAsDefault = highestPriorityAsDefault ?: HIGHEST_PRIORITY_AS_DEFAULT,
    scheduler = scheduler?.toDomain()
)

fun CreateTask.toDto() = CreateTaskDto(
    description = description,
    parentTaskId = parentTaskId,
    priority = priority,
    scheduler = scheduler?.toDto()
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
