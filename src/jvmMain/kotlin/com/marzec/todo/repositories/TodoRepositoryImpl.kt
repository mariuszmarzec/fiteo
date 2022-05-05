package com.marzec.todo.repositories

import com.marzec.core.currentTime
import com.marzec.database.UserEntity
import com.marzec.database.dbCall
import com.marzec.database.findByIdIfBelongsToUserOrThrow
import com.marzec.database.findByIdOrThrow
import com.marzec.database.toSized
import com.marzec.extensions.ifNull
import com.marzec.extensions.listOf
import com.marzec.fiteo.model.domain.User
import com.marzec.todo.TodoRepository
import com.marzec.todo.database.TaskEntity
import com.marzec.todo.database.TasksTable
import com.marzec.todo.extensions.sortTasks
import com.marzec.todo.model.CreateTask
import com.marzec.todo.model.Task
import com.marzec.todo.model.UpdateTask
import kotlinx.datetime.toJavaLocalDateTime
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.selectAll
import java.security.KeyStore.Entry

class TodoRepositoryImpl(private val database: Database) : TodoRepository {

    override fun getTasks(userId: Int): List<Task> = database.dbCall {
        getAllTasks(userId)
    }

    private fun getAllTasks(userId: Int) = TasksTable.selectAll()
        .andWhere { TasksTable.userId.eq(userId) }
        .map { TaskEntity.wrapRow(it).toDomain() }
        .filter { it.parentTaskId == null }
        .sortTasks().map { task ->
            task.copy(subTasks = task.subTasks.sortTasks())
        }

    override fun getScheduledTasks(): Map<User, List<Task>> = database.dbCall {
        TasksTable.selectAll().toList()
            .groupBy { it[TasksTable.userId] }
            .mapKeys { key ->
                UserEntity.findByIdOrThrow(key.key.value).toDomain()
            }.mapValues { entry ->
                entry.value.map {
                    TaskEntity.wrapRow(it).toDomain()
                }.filter { it.scheduler != null }
            }.filter { it.value.isNotEmpty() }
    }

    override fun addTask(userId: Int, task: CreateTask): Task {

        val parentTask = task.parentTaskId?.let { database.dbCall { TaskEntity.findByIdOrThrow(it) } }

        val taskPriority = calcPriority(task, parentTask)

        val taskEntity = database.dbCall {
            TaskEntity.new {
                description = task.description
                isToDo = true
                priority = taskPriority
                scheduler = task.scheduler
                user = UserEntity.findByIdOrThrow(userId)
            }
        }
        return database.dbCall {
            taskEntity.parents = parentTask?.listOf().orEmpty().toSized()
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
        taskEntity.modifiedTime = currentTime().toJavaLocalDateTime()
        taskEntity.scheduler = task.scheduler
        taskEntity.toDomain()
    }

    override fun markAsToDo(userId: Int, isToDo: Boolean, taskIds: List<Int>) = database.dbCall {
        taskIds.forEach { id ->
            val taskEntity = TaskEntity.findByIdOrThrow(id)
            taskEntity.belongsToUserOrThrow(userId)
            taskEntity.isToDo = isToDo
        }
    }

    override fun getTask(userId: Int, id: Int): Task = database.dbCall {
        TaskEntity.findByIdIfBelongsToUserOrThrow(userId, id).toDomain()
    }

    override fun removeTask(userId: Int, taskId: Int): Task = database.dbCall {
        val taskEntity = TaskEntity.findByIdOrThrow(taskId)
        val task = taskEntity.toDomain()
        val subtasks = taskEntity.subtasks
        val parentTask = taskEntity.parents
        taskEntity.deleteIfBelongsToUserOrThrow(userId)
        if (!parentTask.empty()) {
            subtasks.forEach { it.parents = parentTask }
        }
        task
    }

    private fun calcPriority(
        task: CreateTask,
        parentTask: TaskEntity?
    ) = task.priority ?: database.dbCall {
        when {
            parentTask != null -> {
                parentTask.subtasks.toList().takeIf { it.isNotEmpty() }
                    ?.calcPriority(task.highestPriorityAsDefault)
                    .ifNull(parentTask.priority)
            }
            else -> TaskEntity.all().toList().calcPriority(task.highestPriorityAsDefault) ?: 0
        }
    }

    private fun List<TaskEntity>.calcPriority(highestPriorityAsDefault: Boolean) = if (highestPriorityAsDefault) {
        maxOfOrNull { it.priority }?.inc()
    } else {
        minOfOrNull { it.priority }?.dec()
    }
}
