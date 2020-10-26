package com.marzec.todo.database

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.`java-time`.datetime
import java.time.LocalDateTime

object TasksTable : IntIdTable("todo_tasks") {
    val description = text("description")
    val addedTime = datetime("added_time").default(LocalDateTime.now())
    val modifiedTime = datetime("modified_time").default(LocalDateTime.now())
    val isToDo = bool("is_to_do")
    val priority = integer("priority")
}

class TaskEntity(id: EntityID<Int>) : IntEntity(id) {

    var description by TasksTable.description
    var addedTime by TasksTable.addedTime
    var modifiedTime by TasksTable.modifiedTime
    var isToDo by TasksTable.isToDo
    var priority by TasksTable.priority
    var subtasks by TaskEntity.via(TaskToSubtasksTable.parent, TaskToSubtasksTable.child)

    companion object : IntEntityClass<TaskEntity>(TasksTable)
}

object TaskToSubtasksTable : IntIdTable("tasks_to_subtasks") {
    val parent = reference("parent_id", TasksTable)
    val child = reference("child_id", TasksTable)
}