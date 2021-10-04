package com.marzec.todo.database

import com.marzec.database.IntEntityWithUser
import com.marzec.database.UserEntity
import com.marzec.database.UserTable
import com.marzec.todo.model.ToDoList
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object ToDoListTable : IntIdTable("todo_lists") {
    private const val TITLE_LENGTH = 100
    val title = varchar("title", TITLE_LENGTH)
    val userId = reference("user_id", UserTable, onDelete = ReferenceOption.CASCADE)
}

object ToDoListsToTasksTable : IntIdTable("todo_lists_to_tasks") {
    val toDoList = reference("todo_list_id", ToDoListTable, onDelete = ReferenceOption.CASCADE)
    val task = reference("task_id", TasksTable, onDelete = ReferenceOption.CASCADE)
}

class ToDoListEntity(id: EntityID<Int>) : IntEntityWithUser(id) {
    var title by ToDoListTable.title
    var tasks by TaskEntity via ToDoListsToTasksTable
    override var user by UserEntity referencedOn ToDoListTable.userId

    fun toDomain() = ToDoList(
            id = id.value,
            title = title,
            tasks = tasks.toList().filter { it.parents.count() == 0L }.map { it.toDomain() },
    )

    companion object : IntEntityClass<ToDoListEntity>(ToDoListTable)
}
