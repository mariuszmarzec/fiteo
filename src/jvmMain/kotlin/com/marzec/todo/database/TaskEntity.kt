package com.marzec.todo.database

import com.marzec.core.currentTime
import com.marzec.database.ExerciseEntity.Companion.transform
import com.marzec.database.IntEntityWithUser
import com.marzec.database.UserEntity
import com.marzec.database.UserTable
import com.marzec.extensions.formatDate
import kotlinx.datetime.toKotlinLocalDateTime
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.`java-time`.datetime
import com.marzec.todo.extensions.sortTasks
import com.marzec.todo.model.*
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Column

object TasksTable : IntIdTable("todo_tasks") {
    val description = text("description")
    val addedTime = datetime("added_time").apply { defaultValueFun = { currentTime() } }
    val modifiedTime = datetime("modified_time").apply { defaultValueFun = { currentTime() } }

    val isToDo = bool("is_to_do")
    val priority = integer("priority")
    val scheduler = text("scheduler")
    val userId = reference("user_id", UserTable, onDelete = ReferenceOption.CASCADE)
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

    companion object : IntEntityClass<TaskEntity>(TasksTable)
}

object TaskToSubtasksTable : IntIdTable("tasks_to_subtasks") {
    val parent = reference("parent_id", TasksTable, onDelete = ReferenceOption.CASCADE)
    val child = reference("child_id", TasksTable, onDelete = ReferenceOption.CASCADE)
}

fun Column<String>.transformStringSchedulerNullable() = transform(
    toColumn = { scheduler -> scheduler?.toEntity()?.let { Json.encodeToString(it) }.orEmpty() },
    toReal = { value -> value.takeIf { it.isNotEmpty() }?.let { Json.decodeFromString<SchedulerEntity>(it) }?.toDomain() }
)

@Serializable
data class SchedulerEntity(
    val hour: Int,
    val minute: Int,
    val startDate: String,
    val daysOfWeek: List<Int>,
    val dayOfMonth: Int,
    val type: String
)

private fun SchedulerEntity.toDomain(): Scheduler = when(type) {
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

private fun Scheduler.toEntity(): SchedulerEntity = when (this) {
    is Scheduler.OneShot -> SchedulerEntity(
        hour = hour,
        minute = minute,
        startDate = startDate.formatDate(),
        daysOfWeek = emptyList(),
        dayOfMonth = 0,
        type = this::class.simpleName.orEmpty()
    )
    is Scheduler.Weekly -> SchedulerEntity(
        hour = hour,
        minute = minute,
        startDate = startDate.formatDate(),
        daysOfWeek = daysOfWeek.map { it.isoDayNumber },
        dayOfMonth = 0,
        type = this::class.simpleName.orEmpty()
    )
    is Scheduler.Monthly -> SchedulerEntity(
        hour = hour,
        minute = minute,
        startDate = startDate.formatDate(),
        daysOfWeek = emptyList(),
        dayOfMonth = dayOfMonth,
        type = this::class.simpleName.orEmpty()
    )
}