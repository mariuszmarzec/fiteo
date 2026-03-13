package com.marzec

import com.marzec.core.CurrentTimeUtil
import com.marzec.fiteo.model.domain.User
import com.marzec.todo.TodoRepository
import com.marzec.todo.TodoService
import com.marzec.todo.schedule.TaskExpirationDispatcher
import io.mockk.*
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import org.junit.Before
import org.junit.Test
import java.util.TimeZone

class TaskExpirationDispatcherTest {

    val user = User(1, "test@user.com")
    val taskWithExpiration = stubTask(
        id = 1,
        description = "expired task",
        expirationDate = LocalDateTime(2021, 5, 15, 0, 0)
    )
    val taskWithoutExpiration = stubTask(
        id = 2,
        description = "valid task",
        expirationDate = LocalDateTime(2021, 5, 20, 0, 0)
    )

    val repository = mockk<TodoRepository>()
    val service = mockk<TodoService>()

    @Before
    fun setUp() {
        CurrentTimeUtil.init(TimeZone.getTimeZone("Europe/Warsaw"))
        every { service.removeTask(any(), any(), any()) } returns stubTask()
    }

    @Test
    fun `remove expired task`() = runTest {
        CurrentTimeUtil.setOtherTime(16, 5, 2021, 14, 30)
        every { repository.getTasksWithExpirationDate() } returns mapOf(
            user to listOf(taskWithExpiration, taskWithoutExpiration)
        )

        val dispatcher = TaskExpirationDispatcher(repository, service)
        dispatcher.dispatch()

        verify(timeout = 1000) { service.removeTask(user.id, taskWithExpiration.id, removeWithSubtasks = true) }
        verify(inverse = true) { service.removeTask(user.id, taskWithoutExpiration.id, removeWithSubtasks = true) }
    }
}