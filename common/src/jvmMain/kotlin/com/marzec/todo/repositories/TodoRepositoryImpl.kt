package com.marzec.todo.repositories

import com.marzec.core.currentTime
import com.marzec.database.UserEntity
import com.marzec.database.dbCall
import com.marzec.database.findByIdOrThrow
import com.marzec.database.toSized
import com.marzec.extensions.toList
import com.marzec.todo.TodoRepository
import com.marzec.todo.database.TaskEntity
import com.marzec.todo.database.ToDoListEntity
import com.marzec.todo.database.ToDoListTable
import com.marzec.todo.extensions.sortTasks
import com.marzec.todo.model.CreateTask
import com.marzec.todo.model.Task
import com.marzec.todo.model.ToDoList
import com.marzec.todo.model.UpdateTask
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.selectAll

class TodoRepositoryImpl(private val database: Database) : TodoRepository {

    override fun getLists(userId: Int): List<ToDoList> {
        return database.dbCall {
            ToDoListTable.selectAll()
                    .andWhere { ToDoListTable.userId.eq(userId) }
                    .map { ToDoListEntity.wrapRow(it).toDomain() }
                    .map { list ->
                        list.copy(
                                tasks = list.tasks.sortTasks().map { task ->
                                    task.copy(subTasks = task.subTasks.sortTasks())
                                }
                        )
                    }
        }
    }

    override fun addList(userId: Int, listName: String): ToDoList {
        return database.dbCall {
            ToDoListEntity.new {
                title = listName
                user = UserEntity.findByIdOrThrow(userId)
            }.toDomain()
        }
    }

    override fun removeList(userId: Int, listId: Int): ToDoList {
        return database.dbCall {
            val entity = ToDoListEntity.findByIdOrThrow(listId)
            val list = entity.toDomain()
            entity.deleteIfBelongsToUserOrThrow(userId)
            list
        }
    }

    override fun addTask(userId: Int, listId: Int, task: CreateTask): Task {
        val listEntity = database.dbCall {
            val listEntity = ToDoListEntity.findByIdOrThrow(listId)
            listEntity.belongsToUserOrThrow(userId)
            listEntity
        }

        val parentTask = task.parentTaskId?.let { database.dbCall { TaskEntity.findByIdOrThrow(it) } }

        val taskEntity = database.dbCall {
            TaskEntity.new {
                description = task.description
                isToDo = true
                priority = task.priority
                user = UserEntity.findByIdOrThrow(userId)
            }
        }
        return database.dbCall {
            taskEntity.parents = parentTask?.toList().orEmpty().toSized()

            listEntity.tasks = listEntity.tasks.toMutableList().apply { add(taskEntity) }.toSized()
            taskEntity.toDomain()
        }
    }

    override fun updateTask(userId: Int, taskId: Int, task: UpdateTask): Task = database.dbCall {
        val taskEntity = TaskEntity.findByIdOrThrow(taskId)
        taskEntity.belongsToUserOrThrow(userId)
        taskEntity.description = task.description
        taskEntity.parents = task.parentTaskId?.let { listOf(TaskEntity.findByIdOrThrow(it)) }.orEmpty().toSized()
        taskEntity.priority = task.priority
        taskEntity.isToDo = task.isToDo
        taskEntity.modifiedTime = currentTime()
        taskEntity.toDomain()
    }

    override fun removeTask(userId: Int, taskId: Int): Task = database.dbCall {
        val taskEntity = TaskEntity.findByIdOrThrow(taskId)
        val task = taskEntity.toDomain()
        taskEntity.deleteIfBelongsToUserOrThrow(userId)
        task
    }
}