package com.marzec.todo.database

import com.marzec.database.UserTable
import org.jetbrains.exposed.sql.Table

object TaskSharesTable : Table("task_share") {
    val taskId = reference("task_id", TasksTable)
    val userId = reference("user_id", UserTable)
    val ownerId = reference("owner_id", UserTable)
    val permission = varchar("permission", 255)

    override val primaryKey = PrimaryKey(taskId, userId)
}
