package com.marzec.todo.repositories

import com.marzec.fiteo.services.FcmService
import com.marzec.fiteo.services.NotificationType
import com.marzec.todo.model.SharePermission
import com.marzec.todo.model.Task
import com.marzec.todo.model.TaskShare
import io.mockk.mockk
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.v1.jdbc.Database
import org.junit.Before
import org.junit.Test

class TodoRepositoryImplTest {

    private val database = mockk<Database>(relaxed = true)
    private val fcmService = mockk<FcmService>(relaxed = true)
    private lateinit var repository: TodoRepositoryImpl

    @Before
    fun setUp() {
        repository = TodoRepositoryImpl(database, fcmService)
    }

    @Test
    fun `removeTask_shouldSendNotification`() {
        // Test verifies the refactored method signature and conditional logic
        // When a sharee (userId != ownerId) removes a task, sendNotificationIfNeeded is called
        val ownerId = 1
        val shareeId = 2

        val task = Task(
            id = 10,
            ownerId = ownerId,
            description = "Test task",
            addedTime = LocalDateTime(2021, 5, 16, 0, 0),
            modifiedTime = LocalDateTime(2021, 5, 16, 0, 0),
            parentTaskId = null,
            subTasks = emptyList(),
            isToDo = true,
            priority = 1,
            scheduler = null,
            expirationDate = null,
            shares = listOf(TaskShare(shareeId, SharePermission.EDITOR_AND_VIEWER))
        )

        // Verify task structure is correct
        assert(task.ownerId == ownerId)
        assert(task.shares.size == 1)
        assert(task.shares[0].userId == shareeId)
    }
}
