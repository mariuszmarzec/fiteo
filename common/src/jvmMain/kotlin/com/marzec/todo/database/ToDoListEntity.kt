package com.marzec.todo.database

import com.marzec.database.UserEntity
import com.marzec.database.UserTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object ToDoListTable : IntIdTable("todo_lists") {
    val title = varchar("title", 100)
    val userId = reference("user_id", UserTable, onDelete = ReferenceOption.CASCADE)
}

object ToDoListsToTasksTable : IntIdTable("todo_lists_to_tasks") {
    val toDoList = reference("todo_list_id", ToDoListTable)
    val task = reference("task_id", TasksTable)
}

class ToDoListEntity(id: EntityID<Int>) : IntEntity(id) {
    var title by ToDoListTable.title
    var tasks by via(ToDoListsToTasksTable.toDoList, ToDoListsToTasksTable.task)
    val user by via(UserTable)

    companion object : IntEntityClass<UserEntity>(UserTable)
}