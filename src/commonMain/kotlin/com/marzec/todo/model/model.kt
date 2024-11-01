package com.marzec.todo.model

import com.marzec.Api
import com.marzec.Api.Default.HIGHEST_PRIORITY_AS_DEFAULT
import com.marzec.Api.Default.IS_TO_DO_DEFAULT
import com.marzec.extensions.formatDate
import com.marzec.fiteo.model.domain.NullableField
import com.marzec.fiteo.model.dto.NullableFieldDto
import com.marzec.fiteo.model.dto.toDomain
import com.marzec.todo.dto.TaskDto
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlin.reflect.KProperty1

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
    open val creationDate: LocalDateTime? = null,
    open val startDate: LocalDateTime,
    open val lastDate: LocalDateTime?,
    open val repeatCount: Int = DEFAULT_REPEAT_COUNT,
    open val repeatInEveryPeriod: Int = DEFAULT_REPEAT_IN_EVERY_PERIOD,
    open val highestPriorityAsDefault: Boolean
) {
    data class OneShot(
        override val hour: Int,
        override val minute: Int,
        override val creationDate: LocalDateTime?,
        override val startDate: LocalDateTime,
        override val lastDate: LocalDateTime?,
        override val repeatCount: Int = DEFAULT_REPEAT_COUNT,
        override val repeatInEveryPeriod: Int = DEFAULT_REPEAT_IN_EVERY_PERIOD,
        override val highestPriorityAsDefault: Boolean = HIGHEST_PRIORITY_AS_DEFAULT,
        val removeScheduled: Boolean = REMOVE_SCHEDULED
    ) : Scheduler(
        hour = hour,
        minute = minute,
        creationDate = creationDate,
        startDate = startDate,
        lastDate = lastDate,
        repeatCount = repeatCount,
        repeatInEveryPeriod = repeatInEveryPeriod,
        highestPriorityAsDefault = highestPriorityAsDefault
    )

    data class Weekly(
        override val hour: Int,
        override val minute: Int,
        override val creationDate: LocalDateTime?,
        override val startDate: LocalDateTime,
        val daysOfWeek: List<DayOfWeek>,
        override val lastDate: LocalDateTime?,
        override val repeatCount: Int = DEFAULT_REPEAT_COUNT,
        override val repeatInEveryPeriod: Int = DEFAULT_REPEAT_IN_EVERY_PERIOD,
        override val highestPriorityAsDefault: Boolean = HIGHEST_PRIORITY_AS_DEFAULT
    ) : Scheduler(
        hour = hour,
        minute = minute,
        creationDate = creationDate,
        startDate = startDate,
        lastDate = lastDate,
        repeatCount = repeatCount,
        repeatInEveryPeriod = repeatInEveryPeriod,
        highestPriorityAsDefault = highestPriorityAsDefault
    )

    data class Monthly(
        override val hour: Int,
        override val minute: Int,
        override val creationDate: LocalDateTime?,
        override val startDate: LocalDateTime,
        val dayOfMonth: Int,
        override val lastDate: LocalDateTime?,
        override val repeatCount: Int = DEFAULT_REPEAT_COUNT,
        override val repeatInEveryPeriod: Int = DEFAULT_REPEAT_IN_EVERY_PERIOD,
        override val highestPriorityAsDefault: Boolean = HIGHEST_PRIORITY_AS_DEFAULT
    ) : Scheduler(
        hour = hour,
        minute = minute,
        creationDate = creationDate,
        startDate = startDate,
        lastDate = lastDate,
        repeatCount = repeatCount,
        repeatInEveryPeriod = repeatInEveryPeriod,
        highestPriorityAsDefault = highestPriorityAsDefault
    )

    fun updateLastDate(lastDate: LocalDateTime) = when (this) {
        is OneShot -> copy(lastDate = lastDate)
        is Weekly -> copy(lastDate = lastDate)
        is Monthly -> copy(lastDate = lastDate)
    }

    companion object {
        const val DEFAULT_REPEAT_COUNT = -1
        const val DEFAULT_REPEAT_IN_EVERY_PERIOD = 1
        const val HIGHEST_PRIORITY_AS_DEFAULT = Api.Default.HIGHEST_PRIORITY_AS_DEFAULT
        const val REMOVE_SCHEDULED = false
    }
}

@Serializable
data class SchedulerDto(
    val hour: Int,
    val minute: Int,
    val creationDate: String?,
    val startDate: String,
    val lastDate: String? = null,
    val daysOfWeek: List<Int>,
    val dayOfMonth: Int,
    val repeatCount: Int,
    val repeatInEveryPeriod: Int,
    val type: String,
    val options: Map<String, String>? = null
)

val SchedulerDto.highestPriorityAsDefault: Boolean
    get() = getHighestPriorityAsDefault(options)

val SchedulerDto.removeScheduled: Boolean
    get() = getRemoveScheduled(options)

fun getHighestPriorityAsDefault(options: Map<String, String>?) =
    options?.get(Scheduler::highestPriorityAsDefault.name)?.toBooleanStrictOrNull()
        ?: Scheduler.HIGHEST_PRIORITY_AS_DEFAULT

fun getRemoveScheduled(options: Map<String, String>?) =
    options?.get(Scheduler.OneShot::removeScheduled.name)?.toBooleanStrictOrNull()
        ?: Scheduler.REMOVE_SCHEDULED


fun SchedulerDto.toDomain(): Scheduler = when (type) {
    Scheduler.OneShot::class.simpleName -> Scheduler.OneShot(
        hour = hour,
        minute = minute,
        creationDate = creationDate?.toLocalDateTime(),
        startDate = startDate.toLocalDateTime(),
        lastDate = lastDate?.toLocalDateTime(),
        highestPriorityAsDefault = highestPriorityAsDefault,
        removeScheduled = removeScheduled
    )
    Scheduler.Weekly::class.simpleName -> Scheduler.Weekly(
        hour = hour,
        minute = minute,
        creationDate = creationDate?.toLocalDateTime(),
        startDate = startDate.toLocalDateTime(),
        lastDate = lastDate?.toLocalDateTime(),
        daysOfWeek = daysOfWeek.map { DayOfWeek(it) },
        repeatInEveryPeriod = repeatInEveryPeriod,
        repeatCount = repeatCount,
        highestPriorityAsDefault = highestPriorityAsDefault
    )
    Scheduler.Monthly::class.simpleName -> Scheduler.Monthly(
        hour = hour,
        minute = minute,
        creationDate = creationDate?.toLocalDateTime(),
        startDate = startDate.toLocalDateTime(),
        lastDate = lastDate?.toLocalDateTime(),
        dayOfMonth = dayOfMonth,
        repeatInEveryPeriod = repeatInEveryPeriod,
        repeatCount = repeatCount,
        highestPriorityAsDefault = highestPriorityAsDefault
    )
    else -> throw IllegalArgumentException("Unknown type of scheduler")
}

private fun Scheduler.optionsToMap(): Map<String, String>? =
    listOfNotNull(
        takeIfNotDefault(Scheduler::highestPriorityAsDefault, Scheduler.HIGHEST_PRIORITY_AS_DEFAULT)
    ).toMap()
        .takeIf { it.isNotEmpty() }

private fun Scheduler.OneShot.optionsToMap(): Map<String, String>? =
    listOfNotNull(
        takeIfNotDefault(Scheduler::highestPriorityAsDefault, Scheduler.HIGHEST_PRIORITY_AS_DEFAULT),
        takeIfNotDefault(Scheduler.OneShot::removeScheduled, Scheduler.REMOVE_SCHEDULED),
    ).toMap()
        .takeIf { it.isNotEmpty() }

private fun <RECEIVER, VALUE> RECEIVER.takeIfNotDefault(
    kProperty: KProperty1<RECEIVER, VALUE>,
    defaultValue: VALUE
): Pair<String, String>? {
    return kProperty.get(this).takeIf { it != defaultValue }?.let { kProperty.name to it.toString() }
}

fun Scheduler.toDto(): SchedulerDto = when (this) {
    is Scheduler.OneShot -> SchedulerDto(
        hour = hour,
        minute = minute,
        creationDate = creationDate?.formatDate(),
        startDate = startDate.formatDate(),
        lastDate = lastDate?.formatDate(),
        daysOfWeek = emptyList(),
        dayOfMonth = 0,
        repeatInEveryPeriod = repeatInEveryPeriod,
        repeatCount = repeatCount,
        type = this::class.simpleName.orEmpty(),
        options = optionsToMap()
    )
    is Scheduler.Weekly -> SchedulerDto(
        hour = hour,
        minute = minute,
        creationDate = creationDate?.formatDate(),
        startDate = startDate.formatDate(),
        lastDate = lastDate?.formatDate(),
        daysOfWeek = daysOfWeek.map { it.isoDayNumber },
        dayOfMonth = 0,
        repeatInEveryPeriod = repeatInEveryPeriod,
        repeatCount = repeatCount,
        type = this::class.simpleName.orEmpty(),
        options = optionsToMap()
    )
    is Scheduler.Monthly -> SchedulerDto(
        hour = hour,
        minute = minute,
        creationDate = creationDate?.formatDate(),
        startDate = startDate.formatDate(),
        lastDate = lastDate?.formatDate(),
        daysOfWeek = emptyList(),
        dayOfMonth = dayOfMonth,
        repeatInEveryPeriod = repeatInEveryPeriod,
        repeatCount = repeatCount,
        type = this::class.simpleName.orEmpty(),
        options = optionsToMap()
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
    scheduler = scheduler?.toDto()
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
    scheduler = scheduler?.toDomain()
)

data class CreateTask(
    val description: String,
    val parentTaskId: Int?,
    val priority: Int?,
    val highestPriorityAsDefault: Boolean,
    val scheduler: Scheduler?,
    val isToDo: Boolean = true
)

@Serializable
data class CreateTaskDto(
    val description: String,
    val parentTaskId: Int? = null,
    val priority: Int? = null,
    val highestPriorityAsDefault: Boolean? = null,
    val scheduler: SchedulerDto? = null,
    val isToDo: Boolean? = null
)

fun CreateTaskDto.toDomain() = CreateTask(
    description = description,
    parentTaskId = parentTaskId,
    priority = priority,
    highestPriorityAsDefault = highestPriorityAsDefault ?: HIGHEST_PRIORITY_AS_DEFAULT,
    scheduler = scheduler?.toDomain(),
    isToDo = isToDo ?: IS_TO_DO_DEFAULT
)

fun CreateTask.toDto() = CreateTaskDto(
    description = description,
    parentTaskId = parentTaskId,
    priority = priority,
    scheduler = scheduler?.toDto(),
    isToDo = isToDo
)

data class UpdateTask(
    val description: String? = null,
    val parentTaskId: NullableField<Int>? = null,
    val priority: Int? = null,
    val isToDo: Boolean? = null,
    val scheduler: NullableField<Scheduler>? = null
)

@Serializable
data class UpdateTaskDto(
    val description: String? = null,
    val parentTaskId: NullableFieldDto<Int>? = null,
    val priority: Int? = null,
    val isToDo: Boolean? = null,
    val scheduler: NullableFieldDto<SchedulerDto>? = null
)

@Serializable
data class MarkAsToDoDto(
    val isToDo: Boolean,
    val taskIds: List<Int>,
)

fun UpdateTaskDto.toDomain() = UpdateTask(
    description = description,
    parentTaskId = parentTaskId?.toDomain(),
    priority = priority,
    isToDo = isToDo,
    scheduler = scheduler?.toDomain { it?.toDomain() }
)