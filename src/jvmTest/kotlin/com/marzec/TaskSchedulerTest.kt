package com.marzec

import com.marzec.core.CurrentTimeUtil
import com.marzec.di.MILLISECONDS_IN_SECOND
import com.marzec.di.SECONDS_IN_MINUTE
import com.marzec.fiteo.model.domain.User
import com.marzec.todo.TodoRepository
import com.marzec.todo.TodoService
import com.marzec.todo.model.Scheduler
import com.marzec.todo.model.Task
import com.marzec.todo.schedule.SchedulerDispatcher
import io.mockk.*
import kotlinx.datetime.toKotlinLocalDateTime
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.core.context.GlobalContext
import java.time.DayOfWeek
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
    val monthlyScheduler = Scheduler.Monthly(
        hour = 14,
        minute = 20,
        startDate = LocalDateTime.of(2021, 5, 16, 0, 0).toKotlinLocalDateTime(),
        lastDate = null,
        repeatCount = 3,
        repeatInEveryPeriod = 2,
        dayOfMonth = 20
    )
    val monthlyTask = stubTask(
        id = 1,
        description = "1",
        subTasks = listOf(subTask),
        scheduler = monthlyScheduler
    )
    val scheduledMonthlyTasks = taskMap(monthlyTask)

    val scheduledWeeklyTasks = mapOf(
        user to listOf(
            stubTask(
                id = 1,
                description = "1",
                subTasks = listOf(subTask),
                scheduler = Scheduler.Weekly(
                    hour = 14,
                    minute = 20,
                    startDate = LocalDateTime.of(2021, 5, 16, 0, 0).toKotlinLocalDateTime(),
                    lastDate = null,
                    repeatCount = 3,
                    repeatInEveryPeriod = 2,
                    daysOfWeek = listOf(DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
                )
            )
        )
    )
    val scheduledWeeklyTasks2 = mapOf(
        user to listOf(
            stubTask(
                id = 1,
                description = "1",
                subTasks = listOf(subTask),
                scheduler = Scheduler.Weekly(
                    hour = 7,
                    minute = 0,
                    startDate = LocalDateTime.of(2022, 5, 12, 0, 0).toKotlinLocalDateTime(),
                    lastDate = null,
                    repeatCount = -1,
                    repeatInEveryPeriod = 1,
                    daysOfWeek = listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY)
                )
            )
        )
    )
    val scheduledWeeklyTasks3 = mapOf(
        user to listOf(
            stubTask(
                id = 1,
                description = "1",
                subTasks = listOf(subTask),
                scheduler = Scheduler.Weekly(
                    hour = 7,
                    minute = 0,
                    startDate = LocalDateTime.of(2022, 5, 12, 0, 0).toKotlinLocalDateTime(),
                    lastDate = LocalDateTime.of(2022, 5, 19, 0, 0).toKotlinLocalDateTime(),
                    repeatCount = -1,
                    repeatInEveryPeriod = 1,
                    daysOfWeek = listOf(DayOfWeek.FRIDAY)
                )
            )
        )
    )
    val repository = mockk<TodoRepository>()
    val service = mockk<TodoService>()

    @Before
    fun setUp() {
        every { service.copyTask(userId = any(), id = any(), copyPriority = false, copyScheduler = false) } answers {
            stubTask(id = secondArg<Int>().toInt())
        }
        every { service.updateTask(any(), any(), any()) } returns stubTask()
    }

    @Test
    fun `run creation if scheduled for one shot`() {
        CurrentTimeUtil.setOtherTime(16, 5, 2021, 14, 30)
        val dispatcher = schedulerDispatcher(scheduledOneShotTasks)

        dispatcher.dispatch()

        verify {
            service.copyTask(user.id, 1, copyPriority = false, copyScheduler = false)
        }
    }

    @Test
    fun `do not create one shot if scheduled too early`() {
        CurrentTimeUtil.setOtherTime(16, 5, 2021, 14, 36)
        val dispatcher = schedulerDispatcher(scheduledOneShotTasks)

        dispatcher.dispatch()

        verify(inverse = true) { service.copyTask(any(), any()) }
    }

    @Test
    fun `do not create one shot if scheduled too late`() {
        CurrentTimeUtil.setOtherTime(16, 5, 2021, 14, 19)
        val dispatcher = schedulerDispatcher(scheduledOneShotTasks)

        dispatcher.dispatch()

        verify(inverse = true) { service.copyTask(any(), any()) }
    }

    @Test
    fun `run creation if scheduled monthly`() {
        CurrentTimeUtil.setOtherTime(20, 5, 2021, 14, 30)
        val dispatcher = schedulerDispatcher(scheduledMonthlyTasks)

        dispatcher.dispatch()

        verify {
            service.copyTask(userId = user.id, id = 1, copyPriority = false, copyScheduler = false)
        }
    }

    @Test
    fun `run creation if scheduled monthly for last date`() {
        // In november is winter time, one hour backward
        CurrentTimeUtil.setOtherTime(30, 11, 2021, 15, 30)
        val dispatcher =
            schedulerDispatcher(taskMap(monthlyTask.copy(scheduler = monthlyScheduler.copy(repeatCount = 4, dayOfMonth = 30))))

        dispatcher.dispatch()

        verify {
            service.copyTask(userId = user.id, id = 1, copyPriority = false, copyScheduler = false)
        }
    }

    @Test
    fun `run creation if scheduled monthly for last date - case 2`() {
        // In november is winter time, one hour backward
        CurrentTimeUtil.setOtherTime(9, 8, 2022, 8, 13)
        val dispatcher =
            schedulerDispatcher(
                taskMap(
                    monthlyTask.copy(
                        scheduler = monthlyScheduler.copy(
                            hour = 8,
                            minute = 0,
                            startDate = LocalDateTime.of(2021, 5, 16, 0, 0).toKotlinLocalDateTime(),
                            lastDate = null,
                            repeatCount = -1,
                            repeatInEveryPeriod = 1,
                            dayOfMonth = 30
                        )
                    )
                )
            )

        dispatcher.dispatch()

        verify(exactly = 0) {
            service.copyTask(userId = user.id, id = 1, copyPriority = false, copyScheduler = false)
        }
    }

    @Test
    fun `do not create if scheduled monthly and month not in step`() {
        CurrentTimeUtil.setOtherTime(20, 6, 2021, 14, 30)
        val dispatcher = schedulerDispatcher(scheduledMonthlyTasks)

        dispatcher.dispatch()

        verify(inverse = true) {
            service.copyTask(userId = user.id, id = 1, copyPriority = false, copyScheduler = false)
        }
    }

    @Test
    fun `do not create if scheduled monthly and wrong day, but good hour`() {
        CurrentTimeUtil.setOtherTime(21, 5, 2021, 14, 30)
        val dispatcher = schedulerDispatcher(scheduledMonthlyTasks)

        dispatcher.dispatch()

        verify(inverse = true) {
            service.copyTask(userId = user.id, id = 1, copyPriority = false, copyScheduler = false)
        }
    }

    @Test
    fun `do not create if scheduled monthly and not proper time`() {
        CurrentTimeUtil.setOtherTime(21, 6, 2021, 14, 30)
        val dispatcher = schedulerDispatcher(scheduledMonthlyTasks)

        dispatcher.dispatch()

        verify(inverse = true) {
            service.copyTask(userId = user.id, id = 1, copyPriority = false, copyScheduler = false)
        }
    }

    @Test
    fun `do not create if scheduled monthly and wrong hour`() {
        CurrentTimeUtil.setOtherTime(20, 6, 2021, 14, 55)
        val dispatcher = schedulerDispatcher(scheduledMonthlyTasks)

        dispatcher.dispatch()

        verify(inverse = true) {
            service.copyTask(userId = user.id, id = 1, copyPriority = false, copyScheduler = false)
        }
    }

    @Test
    fun `create if scheduled weekly`() {
        CurrentTimeUtil.setOtherTime(19, 5, 2021, 14, 30)
        val dispatcher = schedulerDispatcher(scheduledWeeklyTasks)

        dispatcher.dispatch()

        verify {
            service.copyTask(userId = user.id, id = 1, copyPriority = false, copyScheduler = false)
        }
    }

    @Test
    fun `create if scheduled weekly and today is in 2 week from start date`() {
        CurrentTimeUtil.setOtherTime(2, 6, 2021, 14, 30)
        val dispatcher = schedulerDispatcher(scheduledWeeklyTasks)

        dispatcher.dispatch()

        verify {
            service.copyTask(userId = user.id, id = 1, copyPriority = false, copyScheduler = false)
        }
    }

    @Test
    fun `do not create if scheduled weekly but wrong hour`() {
        verifyDispatcher(scheduledWeeklyTasks, 19, 5, 2021, 14, 55, true)
    }


    @Test
    fun `do not create if scheduled weekly but day out of range`() {
        verifyDispatcher(
            scheduler = scheduledWeeklyTasks,
            day = 27,
            month = 6,
            year = 2021,
            hour = 14,
            minute = 30,
            falseCase = true
        )
    }

    @Test
    fun `create if scheduled weekly, 30 may case`() {
        verifyDispatcher(
            scheduler = scheduledWeeklyTasks2,
            day = 30,
            month = 5,
            year = 2022,
            hour = 7,
            minute = 10,
        )
    }

    @Test
    fun `create if scheduled weekly, 27 may case`() {
        verifyDispatcher(
            scheduler = scheduledWeeklyTasks3,
            day = 27,
            month = 5,
            year = 2022,
            hour = 7,
            minute = 10,
        )
    }

    @Test
    fun `create if scheduled weekly, 31 may case`() {
        verifyDispatcher(
            scheduler = scheduledWeeklyTasks2,
            day = 31,
            month = 5,
            year = 2022,
            hour = 7,
            minute = 10,
        )
    }

    @After
    fun tearDown() {
        GlobalContext.stopKoin()
    }

    private fun verifyDispatcher(
        scheduler: Map<User, List<Task>>,
        day: Int,
        month: Int,
        year: Int,
        hour: Int,
        minute: Int,
        falseCase: Boolean = false
    ) {
        CurrentTimeUtil.setOtherTime(day, month, year, hour, minute)
        val dispatcher = schedulerDispatcher(scheduler)

        dispatcher.dispatch()

        verify(inverse = falseCase) {
            service.copyTask(userId = user.id, id = 1, copyPriority = false, copyScheduler = false)
        }
    }

    private fun schedulerDispatcher(scheduledTasks: Map<User, List<Task>>): SchedulerDispatcher = SchedulerDispatcher(
        todoRepository = repository.apply {
            every { getScheduledTasks() } returns scheduledTasks
        },
        todoService = service,
        schedulerDispatcherInterval = 15 * SECONDS_IN_MINUTE * MILLISECONDS_IN_SECOND,
        timeZoneOffsetHours = 2
    )

    private fun taskMap(task: Task) = mapOf(
        user to listOf(
            task
        )
    )
}