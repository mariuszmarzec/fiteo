package com.marzec.todo.database

import com.marzec.core.currentTime
import com.marzec.database.ExerciseEntity.Companion.transform
import com.marzec.database.IntEntityWithUser
import com.marzec.database.IntEntityWithUserClass
import com.marzec.database.IntIdWithUserTable
import com.marzec.database.UserEntity
import com.marzec.database.UserTable
import com.marzec.extensions.formatDate
import com.marzec.todo.extensions.sortTasks
import com.marzec.todo.model.Scheduler
import com.marzec.todo.model.Task
import com.marzec.todo.model.getHighestPriorityAsDefault
import com.marzec.todo.model.getRemoveScheduled
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.datetime

object TasksTable : IntIdWithUserTable("todo_tasks") {
    val description = text("description")
    val addedTime = datetime("added_time").apply { defaultValueFun = { currentTime().toJavaLocalDateTime() } }
    val modifiedTime = datetime("modified_time").apply { defaultValueFun = { currentTime().toJavaLocalDateTime() } }

    val isToDo = bool("is_to_do")
    val priority = integer("priority")
    val scheduler = text("scheduler")
    override val userId: Column<EntityID<Int>> = reference("user_id", UserTable, onDelete = ReferenceOption.CASCADE)
}

class TaskEntity(id: EntityID<Int>) : IntEntityWithUser(id) {

    var description by TasksTable.description
    var addedTime by TasksTable.addedTime
    var modifiedTime by TasksTable.modifiedTime
    var isToDo by TasksTable.isToDo
    var priority by TasksTable.priority
    var scheduler by TasksTable.scheduler.transformStringSchedulerNullable()
    var parents by TaskEntity.via(TaskToSubtasksTable.child, TaskToSubtasksTable.parent)
    var subtasks by TaskEntity.via(TaskToSubtasksTable.parent, TaskToSubtasksTable.child)
    override var user by UserEntity referencedOn TasksTable.userId

    fun toDomain(): Task {
        return Task(
            id = id.value,
            description = description,
            addedTime = addedTime.toKotlinLocalDateTime(),
            modifiedTime = modifiedTime.toKotlinLocalDateTime(),
            parentTaskId = parents.firstOrNull()?.id?.value,
            subTasks = subtasks.toList().map { it.toDomain() }.sortTasks(),
            isToDo = isToDo,
            priority = priority,
            scheduler = scheduler
        )
    }

    companion object : IntEntityWithUserClass<TaskEntity>(TasksTable)
}

object TaskToSubtasksTable : IntIdTable("tasks_to_subtasks") {
    val parent = reference("parent_id", TasksTable, onDelete = ReferenceOption.CASCADE)
    val child = reference("child_id", TasksTable, onDelete = ReferenceOption.CASCADE)
}

fun Column<String>.transformStringSchedulerNullable() = transform(
    toColumn = { scheduler -> scheduler?.toEntity()?.let { Json.encodeToString(it) }.orEmpty() },
    toReal = { value ->
        value.takeIf { it.isNotEmpty() }?.let { Json.decodeFromString<SchedulerEntity>(it) }?.toDomain()
    }
)

@Serializable
data class SchedulerEntity(
    val hour: Int,
    val minute: Int,
    val startDate: String,
    val lastDate: String?,
    val daysOfWeek: List<Int>,
    val dayOfMonth: Int,
    val repeatCount: Int,
    val repeatInEveryPeriod: Int,
    val type: String,
    val options: Map<String, String>? = null
)

val SchedulerEntity.highestPriorityAsDefault: Boolean
    get() = getHighestPriorityAsDefault(options)

val SchedulerEntity.removeScheduled: Boolean
    get() = getRemoveScheduled(options)

fun SchedulerEntity.toDomain(): Scheduler = when (type) {
    Scheduler.OneShot::class.simpleName -> Scheduler.OneShot(
        hour = hour,
        minute = minute,
        startDate = startDate.toLocalDateTime(),
        lastDate = lastDate?.toLocalDateTime(),
        highestPriorityAsDefault = highestPriorityAsDefault,
        removeScheduled = removeScheduled
    )
    Scheduler.Weekly::class.simpleName -> Scheduler.Weekly(
        hour = hour,
        minute = minute,
        startDate = startDate.toLocalDateTime(),
        lastDate = lastDate?.toLocalDateTime(),
        daysOfWeek = daysOfWeek.map { DayOfWeek(it) },
        repeatInEveryPeriod = repeatInEveryPeriod,
        repeatCount = repeatCount,
        highestPriorityAsDefault = highestPriorityAsDefault,
    )
    Scheduler.Monthly::class.simpleName -> Scheduler.Monthly(
        hour = hour, minute = minute, startDate = startDate.toLocalDateTime(),
        lastDate = lastDate?.toLocalDateTime(),
        dayOfMonth = dayOfMonth,
        repeatInEveryPeriod = repeatInEveryPeriod,
        repeatCount = repeatCount,
        highestPriorityAsDefault = highestPriorityAsDefault,
    )
    else -> throw IllegalArgumentException("Unknown type of scheduler")
}

fun Scheduler.toEntity(): SchedulerEntity = when (this) {
    is Scheduler.OneShot -> SchedulerEntity(
        hour = hour,
        minute = minute,
        startDate = startDate.formatDate(),
        lastDate = lastDate?.formatDate(),
        daysOfWeek = emptyList(),
        dayOfMonth = 0,
        repeatInEveryPeriod = repeatInEveryPeriod,
        repeatCount = repeatCount,
        type = this::class.simpleName.orEmpty(),
        options = mapOf(
            Scheduler::highestPriorityAsDefault.name to highestPriorityAsDefault.toString(),
            Scheduler.OneShot::removeScheduled.name to removeScheduled.toString()
        )
    )
    is Scheduler.Weekly -> SchedulerEntity(
        hour = hour,
        minute = minute,
        startDate = startDate.formatDate(),
        lastDate = lastDate?.formatDate(),
        daysOfWeek = daysOfWeek.map { it.isoDayNumber },
        dayOfMonth = 0,
        repeatInEveryPeriod = repeatInEveryPeriod,
        repeatCount = repeatCount,
        type = this::class.simpleName.orEmpty(),
        options = mapOf(
            Scheduler::highestPriorityAsDefault.name to highestPriorityAsDefault.toString(),
        )
    )
    is Scheduler.Monthly -> SchedulerEntity(
        hour = hour,
        minute = minute,
        startDate = startDate.formatDate(),
        lastDate = lastDate?.formatDate(),
        daysOfWeek = emptyList(),
        dayOfMonth = dayOfMonth,
        repeatInEveryPeriod = repeatInEveryPeriod,
        repeatCount = repeatCount,
        type = this::class.simpleName.orEmpty(),
        options = mapOf(
            Scheduler::highestPriorityAsDefault.name to highestPriorityAsDefault.toString(),
        )

    )
}
