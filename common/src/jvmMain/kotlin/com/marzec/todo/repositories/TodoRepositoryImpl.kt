package com.marzec.todo.repositories

import com.marzec.database.UserEntity
import com.marzec.database.dbCall
import com.marzec.database.findByIdOrThrow
import com.marzec.database.toSized
import com.marzec.extensions.toList
import com.marzec.todo.database.TaskEntity
import com.marzec.todo.database.ToDoListEntity
import com.marzec.todo.database.ToDoListTable
import com.marzec.todo.extensions.sortTasks
import com.marzec.todo.model.CreateTask
import com.marzec.todo.model.Task
import com.marzec.todo.model.ToDoList
import com.marzec.todo.model.UpdateTask
import java.time.LocalDateTime
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.selectAll

class TodoRepositoryImpl : TodoRepository {

    override fun getLists(userId: Int): List<ToDoList> {
        return dbCall {
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
            val list = entity.toDomain()
            entity.deleteIfBelongsToUserOrThrow(userId)
            list
        }
    }

    override fun addTask(userId: Int, listId: Int, task: CreateTask): Task {
        val listEntity = dbCall {
            val listEntity = ToDoListEntity.findByIdOrThrow(listId)
            listEntity.belongsToUserOrThrow(userId)
            listEntity
        }

        val parentTask = task.parentTaskId?.let { TaskEntity.findByIdOrThrow(it) }

        val taskEntity = dbCall {
            TaskEntity.new {
                description = task.description
                isToDo = true
                priority = task.priority
                user = UserEntity.findByIdOrThrow(userId)
            }
        }
        return dbCall {
            taskEntity.parents = parentTask?.toList().orEmpty().toSized()

            listEntity.tasks = listEntity.tasks.toMutableList().apply { add(taskEntity) }.toSized()
            taskEntity.toDomain()
        }
    }

    override fun updateTask(userId: Int, taskId: Int, task: UpdateTask): Task = dbCall {
        val taskEntity = TaskEntity.findByIdOrThrow(taskId)
        taskEntity.belongsToUserOrThrow(userId)
        taskEntity.description = task.description
        taskEntity.parents = task.parentTaskId?.let { listOf(TaskEntity.findByIdOrThrow(it)) }.orEmpty().toSized()
        taskEntity.priority = task.priority
        taskEntity.isToDo = task.isToDo
        taskEntity.modifiedTime = LocalDateTime.now()
        taskEntity.toDomain()
    }

    override fun removeTask(userId: Int, taskId: Int): Task = dbCall {
        val taskEntity = TaskEntity.findByIdOrThrow(taskId)
        val task = taskEntity.toDomain()
        taskEntity.deleteIfBelongsToUserOrThrow(userId)
        task
    }
}