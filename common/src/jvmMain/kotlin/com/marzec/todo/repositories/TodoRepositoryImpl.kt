package com.marzec.todo.repositories

import com.marzec.database.UserEntity
import com.marzec.database.dbCall
import com.marzec.database.findByIdOrThrow
import com.marzec.database.toSized
import com.marzec.todo.database.TaskEntity
import com.marzec.todo.database.ToDoListEntity
import com.marzec.todo.database.ToDoListTable
import com.marzec.todo.model.CreateTask
import com.marzec.todo.model.Task
import com.marzec.todo.model.ToDoList
import java.time.LocalDateTime
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class TodoRepositoryImpl : TodoRepository {

    override fun getLists(userId: Int): List<ToDoList> {
        return dbCall {
            ToDoListTable.selectAll()
                    .andWhere { ToDoListTable.userId.eq(userId) }
                    .map { ToDoListEntity.wrapRow(it).toDomain() }
        }
    }

    override fun addList(userId: Int, listName: String): ToDoList {
        return dbCall {
            ToDoListEntity.new {
                title = listName
                user = UserEntity.findByIdOrThrow(userId)
            }.toDomain()
        }
    }

    override fun removeList(userId: Int, listId: Int): ToDoList {
        return dbCall {
            val entity = ToDoListEntity.findByIdOrThrow(listId)
            entity.deleteIfBelongsToUserOrThrow(userId)
            entity.toDomain()
        }
    }

    override fun addTask(userId: Int, listId: Int, task: CreateTask): Task {
        val listEntity = transaction {
            val listEntity = ToDoListEntity.findByIdOrThrow(listId)
            listEntity.belongsToUserOrThrow(userId)
            listEntity
        }

        val taskEntity = dbCall {
            TaskEntity.new {
                description = task.description
                isToDo = true
                priority = task.priority
                user = UserEntity.findByIdOrThrow(userId)
            }
        }
        return dbCall {
            taskEntity.parents = task.parentTaskId?.let { listOf(TaskEntity.findByIdOrThrow(it)) }.orEmpty().toSized()

            listEntity.tasks = listEntity.tasks.toMutableList().apply { add(taskEntity) }.toSized()
            taskEntity.toDomain()
        }
    }
}