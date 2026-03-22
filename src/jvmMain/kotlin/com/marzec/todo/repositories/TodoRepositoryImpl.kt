package com.marzec.todo.repositories

import com.marzec.core.currentTime
import com.marzec.database.UserEntity
import com.marzec.database.dbCall
import com.marzec.database.findByIdOrThrow
import com.marzec.database.toSized
import com.marzec.extensions.ifNull
import com.marzec.extensions.listOf
import com.marzec.extensions.update
import com.marzec.extensions.updateByNullable
import com.marzec.extensions.updateNullable
import com.marzec.fiteo.model.domain.NullableField
import com.marzec.fiteo.model.domain.User
import com.marzec.fiteo.services.FcmService
import com.marzec.fiteo.services.NotificationType
import com.marzec.todo.TodoRepository
import com.marzec.todo.database.TaskEntity
import com.marzec.todo.database.TaskSharesTable
import com.marzec.todo.database.TasksTable
import com.marzec.todo.extensions.sortTasks
import com.marzec.todo.model.CreateTask
import com.marzec.todo.model.SharePermission
import com.marzec.todo.model.Task
import com.marzec.todo.model.TaskShare
import com.marzec.todo.model.UpdateTask
import com.marzec.todo.model.toDto
import kotlinx.datetime.toJavaLocalDateTime
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class TodoRepositoryImpl(
    private val database: Database,
    private val fcmService: FcmService,
) : TodoRepository {

    override fun getTasks(userId: Int): List<Task> = database.dbCall {
        val tasksFromShares = TaskSharesTable.innerJoin(TasksTable)
            .selectAll().where { TaskSharesTable.userId.eq(userId) }
            .map { TaskEntity.wrapRow(it).toDomain(getShares(it[TasksTable.id].value)) }

        val regularTasks = getAllTasks(userId)

        (tasksFromShares + regularTasks).distinctBy { it.id }.sortTasks()
    }

    private fun getAllTasks(userId: Int) = TasksTable.selectAll()
        .andWhere { TasksTable.userId.eq(userId) }
        .map { TaskEntity.wrapRow(it).toDomain(getShares(it[TasksTable.id].value)) }
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
                    TaskEntity.wrapRow(it).toDomain(getShares(it[TasksTable.id].value))
                }.filter { it.scheduler != null }
            }.filter { it.value.isNotEmpty() }
    }

    override fun getTasksWithExpirationDate(): Map<User, List<Task>> = database.dbCall {
        TasksTable.selectAll().toList()
            .groupBy { it[TasksTable.userId] }
            .mapKeys { key ->
                UserEntity.findByIdOrThrow(key.key.value).toDomain()
            }.mapValues { entry ->
                entry.value.map {
                    TaskEntity.wrapRow(it).toDomain(getShares(it[TasksTable.id].value))
                }.filter { it.expirationDate != null }
            }.filter { it.value.isNotEmpty() }
    }

    override fun addTask(userId: Int, task: CreateTask): Task = database.dbCall {
        val parentTask = task.parentTaskId?.let { TaskEntity.findByIdOrThrow(it) }

        if (parentTask != null && parentTask.user.id.value != userId) {
            val shares = getShares(parentTask.id.value)
            val share = shares.find { it.userId == userId }
            if (share?.permission != SharePermission.EDITOR_AND_VIEWER) {
                throw NoSuchElementException("Action not permitted due to lack of editor permission")
            }
        }

        val taskPriority = task.priority ?: calcPriority(task, parentTask)

        val taskEntity = TaskEntity.new {
            description = task.description
            isToDo = true
            priority = taskPriority
            scheduler = task.scheduler
            expirationDate = task.expirationDate?.toJavaLocalDateTime()
            isToDo = task.isToDo
            user = UserEntity.findByIdOrThrow(userId)
        }
        addShares(taskEntity.id.value, userId, task.shares)
        taskEntity.parents = parentTask?.listOf().orEmpty().toSized()
        taskEntity.toDomain(getShares(taskEntity.id.value))
    }

    override fun markAsToDo(userId: Int, isToDo: Boolean, taskIds: List<Int>) = database.dbCall {
        taskIds.forEach { id ->
            val taskEntity = TaskEntity.findByIdOrThrow(id)
            if (taskEntity.user.id.value != userId) {
                val shares = getShares(id)
                val share = shares.find { it.userId == userId }
                if (share?.permission != SharePermission.EDITOR_AND_VIEWER) {
                    throw NoSuchElementException("Action not permitted due to lack of editor permission")
                }
            }
            taskEntity.isToDo = isToDo
        }
    }

    override fun getTask(userId: Int, id: Int): Task = database.dbCall {
        val taskEntity = TaskEntity.findByIdOrThrow(id)
        if (taskEntity.user.id.value != userId && getShares(id).none { it.userId == userId }) {
            throw NoSuchElementException("Task not found")
        }
        taskEntity.toDomain(getShares(taskEntity.id.value))
    }

    override fun updateTask(userId: Int, taskId: Int, task: UpdateTask): Task = database.dbCall {
        val taskEntity = TaskEntity.findByIdOrThrow(taskId)
        val currentShares = getShares(taskId)
        val ownerId = taskEntity.user.id.value

        if (ownerId == userId) {
            taskEntity.apply {
                update(this::description, task.description)
                updateByNullable(this::parents, task.parentTaskId) {
                    it?.let { listOf(TaskEntity.findByIdOrThrow(it)) }.orEmpty().toSized()
                }
                update(this::priority, task.priority)
                update(this::isToDo, task.isToDo)
                update(this::modifiedTime, currentTime().toJavaLocalDateTime())
                updateNullable(this::scheduler, task.scheduler)
                task.expirationDate?.let { field ->
                    updateNullable(this::expirationDate, NullableField(field.value?.toJavaLocalDateTime()))
                }
            }
            task.shares?.let { updateShares(taskId, ownerId, it) }
        } else {
            val share = currentShares.find { it.userId == userId }

            if (share?.permission == SharePermission.EDITOR_AND_VIEWER) {
                taskEntity.apply {
                    update(this::description, task.description)
                    updateByNullable(this::parents, task.parentTaskId) {
                        it?.let { listOf(TaskEntity.findByIdOrThrow(it)) }.orEmpty().toSized()
                    }
                    update(this::priority, task.priority)
                    update(this::isToDo, task.isToDo)
                    update(this::modifiedTime, currentTime().toJavaLocalDateTime())
                    updateNullable(this::scheduler, task.scheduler)
                    task.expirationDate?.let { field ->
                        updateNullable(this::expirationDate, NullableField(field.value?.toJavaLocalDateTime()))
                    }
                }
            } else {
                throw NoSuchElementException("Action not permitted due to lack of editor permission")
            }
        }
        taskEntity.toDomain(getShares(taskId))
    }

    override fun leaveShare(userId: Int, taskId: Int): Task = database.dbCall {
        val taskEntity = TaskEntity.findByIdOrThrow(taskId)
        removeShare(taskId, userId)
        taskEntity.toDomain(getShares(taskId))
    }

    private fun updateShares(
        taskId: Int,
        ownerId: Int,
        sharesToUpdate: List<TaskShare>
    ) {
        val currentShares = getShares(taskId)
        currentShares.forEach { share ->
            removeShare(taskId, share.userId)
        }
        addShares(taskId, ownerId, sharesToUpdate)
    }

    private fun removeShare(taskId: Int, userId: Int) {
        TaskSharesTable.deleteWhere { TaskSharesTable.taskId.eq(taskId) and TaskSharesTable.userId.eq(userId) }
    }

    private fun addShares(taskId: Int, ownerId: Int, shares: List<TaskShare>) {
        shares.forEach { share ->
            TaskSharesTable.insert {
                it[TaskSharesTable.taskId] = taskId
                it[TaskSharesTable.userId] = share.userId
                it[TaskSharesTable.ownerId] = ownerId
                it[TaskSharesTable.permission] = share.permission.name
            }
        }
    }

    private fun getShares(taskId: Int): List<TaskShare> {
        return TaskSharesTable.selectAll().where { TaskSharesTable.taskId.eq(taskId) }.map {
            TaskShare(
                userId = it[TaskSharesTable.userId].value,
                permission = SharePermission.valueOf(it[TaskSharesTable.permission].uppercase())
            )
        }
    }

    override fun removeTask(userId: Int, taskId: Int, removeWithSubtasks: Boolean): Task = database.dbCall {
        val taskEntity = TaskEntity.findByIdOrThrow(taskId)
        
        if (taskEntity.user.id.value != userId) {
            val shares = getShares(taskId)
            val share = shares.find { it.userId == userId }
            if (share?.permission != SharePermission.EDITOR_AND_VIEWER) {
                throw NoSuchElementException("Action not permitted due to lack of editor permission")
            }
        }

        val task = taskEntity.toDomain(getShares(taskId))
        removeInternal(taskEntity, userId, removeWithSubtasks)
        sendNotificationIfNeeded(task, userId, task)
        task
    }

    private fun sendNotificationIfNeeded(
        removedTask: Task,
        userId: Int,
        task: Task
    ) {
        if (removedTask.scheduler?.showNotification == true) {
            val removedTaskDto = removedTask.toDto()
            fcmService.sendPushNotification(userId, removedTaskDto, NotificationType.TASK_REMOVED)
            task.shares.forEach { share ->
                fcmService.sendPushNotification(share.userId, removedTaskDto, NotificationType.TASK_REMOVED)
            }
        }
    }

    private fun removeInternal(taskEntity: TaskEntity, userId: Int, removeWithSubtasks: Boolean) {
        val subtasks = taskEntity.subtasks
        val parentTask = taskEntity.parents
        if (removeWithSubtasks) {
            subtasks.forEach {
                it.parents = emptySized()
                removeInternal(it, userId, removeWithSubtasks = true)
            }
        } else {
            if (!parentTask.empty()) {
                subtasks.forEach { it.parents = parentTask }
            }
        }
        taskEntity.delete() // Changed from deleteIfBelongsToUserOrThrow since we check permissions above
    }

    private fun calcPriority(
        task: CreateTask,
        parentTask: TaskEntity?
    ) = when {
        parentTask != null -> {
            parentTask.subtasks.toList().takeIf { it.isNotEmpty() }
                ?.calcPriority(task.highestPriorityAsDefault)
                .ifNull(parentTask.priority)
        }
        else -> TaskEntity.all().toList().calcPriority(task.highestPriorityAsDefault) ?: 0
    }

    private fun List<TaskEntity>.calcPriority(highestPriorityAsDefault: Boolean) = if (highestPriorityAsDefault) {
        maxOfOrNull { it.priority }?.inc()
    } else {
        minOfOrNull { it.priority }?.dec()
    }
}
