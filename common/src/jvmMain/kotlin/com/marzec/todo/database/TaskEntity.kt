package com.marzec.todo.database

import com.marzec.database.UserEntity
import com.marzec.database.UserTable
import com.marzec.todo.model.Task
import java.time.LocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.`java-time`.datetime

object TasksTable : IntIdTable("todo_tasks") {
    val description = text("description")
    val addedTime = datetime("added_time").default(LocalDateTime.now())
    val modifiedTime = datetime("modified_time").default(LocalDateTime.now())
    val isToDo = bool("is_to_do")
    val priority = integer("priority")
    val userId = reference("user_id", UserTable, onDelete = ReferenceOption.CASCADE)
}

class TaskEntity(id: EntityID<Int>) : IntEntity(id) {

    var description by TasksTable.description
    var addedTime by TasksTable.addedTime
    var modifiedTime by TasksTable.modifiedTime
    var isToDo by TasksTable.isToDo
    var priority by TasksTable.priority
    var parents by TaskEntity.via(TaskToSubtasksTable.child, TaskToSubtasksTable.parent)
    var subtasks by TaskEntity.via(TaskToSubtasksTable.parent, TaskToSubtasksTable.child)
    val user by UserEntity via TasksTable

    fun toDomain(): Task {
        val parentTask = parents.firstOrNull()?.toDomain()
        return Task(
                id = id.value,
                description = description,
                addedTime = addedTime.toKotlinLocalDateTime(),
                modifiedTime = modifiedTime.toKotlinLocalDateTime(),
                parentTask = parentTask,
                subTasks = subtasks.toList().map { it.toDomain() },
                isToDo = isToDo,
                priority = priority
        )
    }

    companion object : IntEntityClass<TaskEntity>(TasksTable)
}

object TaskToSubtasksTable : IntIdTable("tasks_to_subtasks") {
    val parent = reference("parent_id", TasksTable)
    val child = reference("child_id", TasksTable)
}