package com.marzec.todo.database

import com.marzec.core.currentTime
import com.marzec.database.IntEntityWithUser
import com.marzec.database.UserEntity
import com.marzec.database.UserTable
import com.marzec.todo.model.Task
import kotlinx.datetime.toKotlinLocalDateTime
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.`java-time`.datetime
import com.marzec.todo.extensions.sortTasks

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
    var scheduler by TasksTable.scheduler
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
                priority = priority
        )
    }

    companion object : IntEntityClass<TaskEntity>(TasksTable)
}

object TaskToSubtasksTable : IntIdTable("tasks_to_subtasks") {
    val parent = reference("parent_id", TasksTable, onDelete = ReferenceOption.CASCADE)
    val child = reference("child_id", TasksTable, onDelete = ReferenceOption.CASCADE)
}
