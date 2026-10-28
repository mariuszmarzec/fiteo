package com.marzec.todo.repositories

import com.marzec.database.DbSettings
import com.marzec.fiteo.services.FcmService
import com.marzec.fiteo.services.NotificationType
import com.marzec.todo.database.TaskSharesTable
import com.marzec.todo.database.TasksTable
import com.marzec.todo.model.SharePermission
import com.marzec.todo.model.TaskShare
import com.marzec.todo.model.toDto
import com.marzec.database.UserTable
import com.marzec.todo.database.TaskToSubtasksTable
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDateTime
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.junit.Before
import org.junit.Test

class TodoRepositoryImplTest {

    private val database = Database.connect(
        url = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL",
        driver = "org.h2.Driver"
    )
    private val fcmService = mockk<FcmService>(relaxed = true)
    private lateinit var repository: TodoRepositoryImpl

    @Before
    fun setUp() {
        transaction(database) {
            SchemaUtils.create(UserTable, TasksTable, TaskToSubtasksTable, TaskSharesTable)
            UserTable.insert {
                it[UserTable.id] = 1
                it[UserTable.email] = "owner@test.com"
                it[UserTable.password] = "password"
            }
            UserTable.insert {
                it[UserTable.id] = 2
                it[UserTable.email] = "sharee@test.com"
                it[UserTable.password] = "password"
            }
            TasksTable.insert {
                it[TasksTable.id] = 10
                it[TasksTable.description] = "Test task"
                it[TasksTable.addedTime] = LocalDateTime(2021, 5, 16, 0, 0).toJavaLocalDateTime()
                it[TasksTable.modifiedTime] = LocalDateTime(2021, 5, 16, 0, 0).toJavaLocalDateTime()
                it[TasksTable.isToDo] = true
                it[TasksTable.priority] = 1
                it[TasksTable.scheduler] = ""
                it[TasksTable.userId] = 1
            }
            TaskSharesTable.insert {
                it[TaskSharesTable.taskId] = 10
                it[TaskSharesTable.userId] = 2
                it[TaskSharesTable.ownerId] = 1
                it[TaskSharesTable.permission] = "EDITOR_AND_VIEWER"
            }
        }
        repository = TodoRepositoryImpl(database, fcmService)
    }

    @Test
    fun `removeTask_shouldSendNotification()`() {
        val shareeId = 2
        val taskId = 10

        val removedTask = repository.removeTask(shareeId, taskId, removeWithSubtasks = false)

        verify(exactly = 1) {
            fcmService.sendPushNotification(removedTask.ownerId, removedTask.toDto(), NotificationType.TASK_REMOVED)
        }
        verify(exactly = 1) {
            fcmService.sendPushNotification(shareeId, removedTask.toDto(), NotificationType.TASK_REMOVED)
        }
    }
}
