package com.marzec

import com.marzec.core.CurrentTimeUtil
import com.marzec.di.MILLISECONDS_IN_SECOND
import com.marzec.di.SECONDS_IN_MINUTE
import com.marzec.fiteo.model.domain.User
import com.marzec.todo.TodoRepository
import com.marzec.todo.model.CreateTask
import com.marzec.todo.model.Scheduler
import com.marzec.todo.model.Task
import com.marzec.todo.schedule.SchedulerDispatcher
import io.mockk.*
import kotlinx.datetime.toKotlinLocalDateTime
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

class TaskSchedulerTest {

    val user = User(1, "test@user.com")
    val subTask = stubTask(id = 2, description = "2")
    val scheduledOneShotTasks = mapOf(
        user to listOf(
            stubTask(
                id = 1,
                description = "1",
                subTasks = listOf(subTask),
                scheduler = Scheduler.OneShot(
                    hour = 14,
                    minute = 20,
                    LocalDateTime.of(2021, 5, 16, 0, 0).toKotlinLocalDateTime(),
                    lastDate = null,
                    repeatCount = -1,
                    repeatInEveryPeriod = 1
                )
            )
        )
    )
    val scheduledMonthlyTasks = mapOf(
        user to listOf(
            stubTask(
                id = 1,
                description = "1",
                subTasks = listOf(subTask),
                scheduler = Scheduler.Monthly(
                    hour = 14,
                    minute = 20,
                    startDate = LocalDateTime.of(2021, 5, 16, 0, 0).toKotlinLocalDateTime(),
                    lastDate = null,
                    repeatCount = 3,
                    repeatInEveryPeriod = 2,
                    dayOfMonth = 20
                )
            )
        )
    )
    val repository = mockk<TodoRepository>()

    @Before
    fun setUp() {
        every { repository.addTask(any(), any()) } answers {
            stubTask(id = secondArg<CreateTask>().description.toInt())
        }
        every { repository.updateTask(any(), any(), any()) } returns stubTask()
    }

    @Test
    fun `run creation if scheduled for one shot`() {
        CurrentTimeUtil.setOtherTime(16, 5, 2021, 14, 30)
        val dispatcher = schedulerDispatcher(scheduledOneShotTasks)

        dispatcher.dispatch()

        verify {
            repository.addTask(user.id, stubCreateTask(description = "1"))
            repository.addTask(user.id, stubCreateTask(description = "2", parentTaskId = 1))
        }
    }

    @Test
    fun `do not create one shot if scheduled too early`() {
        CurrentTimeUtil.setOtherTime(16, 5, 2021, 14, 36)
        val dispatcher = schedulerDispatcher(scheduledOneShotTasks)

        dispatcher.dispatch()

        verify(inverse = true) { repository.addTask(any(), any()) }
    }

    @Test
    fun `do not create one shot if scheduled too late`() {
        CurrentTimeUtil.setOtherTime(16, 5, 2021, 14, 19)
        val dispatcher = schedulerDispatcher(scheduledOneShotTasks)

        dispatcher.dispatch()

        verify(inverse = true) { repository.addTask(any(), any()) }
    }

    @Test
    fun `run creation if scheduled monthly`() {
        CurrentTimeUtil.setOtherTime(20, 5, 2021, 14, 30)
        val dispatcher = schedulerDispatcher(scheduledMonthlyTasks)

        dispatcher.dispatch()

        verify {
            repository.addTask(user.id, stubCreateTask(description = "1"))
            repository.addTask(user.id, stubCreateTask(description = "2", parentTaskId = 1))
        }
    }

    @Test
    fun `run creation if scheduled monthly for last date`() {
        // In november is winter time, one hour backward
        CurrentTimeUtil.setOtherTime(20, 11, 2021, 15, 30)
        val dispatcher = schedulerDispatcher(scheduledMonthlyTasks)

        dispatcher.dispatch()

        verify {
            repository.addTask(user.id, stubCreateTask(description = "1"))
            repository.addTask(user.id, stubCreateTask(description = "2", parentTaskId = 1))
        }
    }

    @Test
    fun `do not create if scheduled monthly and month not in step`() {
        CurrentTimeUtil.setOtherTime(20, 6, 2021, 14, 30)
        val dispatcher = schedulerDispatcher(scheduledMonthlyTasks)

        dispatcher.dispatch()

        verify(inverse = true) {
            repository.addTask(user.id, stubCreateTask(description = "1"))
            repository.addTask(user.id, stubCreateTask(description = "2", parentTaskId = 1))
        }
    }

    @Test
    fun `do not create if scheduled monthly and wrong day`() {
        CurrentTimeUtil.setOtherTime(21, 6, 2021, 14, 30)
        val dispatcher = schedulerDispatcher(scheduledMonthlyTasks)

        dispatcher.dispatch()

        verify(inverse = true) {
            repository.addTask(user.id, stubCreateTask(description = "1"))
            repository.addTask(user.id, stubCreateTask(description = "2", parentTaskId = 1))
        }
    }

    @Test
    fun `do not create if scheduled monthly and wrong hour`() {
        CurrentTimeUtil.setOtherTime(20, 6, 2021, 14, 55)
        val dispatcher = schedulerDispatcher(scheduledMonthlyTasks)

        dispatcher.dispatch()

        verify(inverse = true) {
            repository.addTask(user.id, stubCreateTask(description = "1"))
            repository.addTask(user.id, stubCreateTask(description = "2", parentTaskId = 1))
        }
    }

    private fun schedulerDispatcher(scheduledTasks: Map<User, List<Task>>): SchedulerDispatcher = SchedulerDispatcher(
        todoRepository = repository.apply {
            every { getScheduledTasks() } returns scheduledTasks
        },
        schedulerDispatcherInterval = 15 * SECONDS_IN_MINUTE * MILLISECONDS_IN_SECOND,
        timeZoneOffsetHours = 2
    )
}