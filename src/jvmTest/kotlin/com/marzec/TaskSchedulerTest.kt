package com.marzec

import com.marzec.core.CurrentTimeUtil
import com.marzec.di.MILLISECONDS_IN_SECOND
import com.marzec.di.SECONDS_IN_MINUTE
import com.marzec.events.EventBus
import com.marzec.fiteo.model.domain.User
import com.marzec.fiteo.services.FcmService
import com.marzec.todo.TodoRepository
import com.marzec.todo.TodoService
import com.marzec.todo.model.Scheduler
import com.marzec.todo.model.Task
import com.marzec.todo.schedule.SchedulerDispatcher
import io.mockk.*
import kotlinx.coroutines.test.runTest
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
                    creationDate = null,
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
        creationDate = null,
        startDate = LocalDateTime.of(2021, 5, 16, 0, 0).toKotlinLocalDateTime(),
        lastDate = null,
        repeatCount = 3,
        repeatInEveryPeriod = 2,
        dayOfMonth = 20
    )

    val weeklyScheduler = Scheduler.Weekly(
        hour = 14,
        minute = 20,
        creationDate = null,
        startDate = LocalDateTime.of(2021, 5, 16, 0, 0).toKotlinLocalDateTime(),
        lastDate = null,
        repeatCount = 3,
        repeatInEveryPeriod = 2,
        daysOfWeek = listOf(DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
    )

    val scheduledWeeklyTasks = mapOf(
        user to listOf(
            stubTask(
                id = 1,
                description = "1",
                subTasks = listOf(subTask),
                scheduler = weeklyScheduler
            )
        )
    )
    val scheduledWeeklyTasksWithLastDay = mapOf(
        user to listOf(
            stubTask(
                id = 1,
                description = "1",
                subTasks = listOf(subTask),
                scheduler = Scheduler.Weekly(
                    hour = 14,
                    minute = 20,
                    creationDate = null,
                    startDate = LocalDateTime.of(2021, 5, 16, 0, 0).toKotlinLocalDateTime(),
                    lastDate = LocalDateTime.of(2021, 6, 16, 0, 0).toKotlinLocalDateTime(),
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
                    creationDate = null,
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
                    creationDate = null,
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
    val fcmService = mockk<FcmService>(relaxed = true)

    @Before
    fun setUp() {
        every { service.copyTask(userId = any(), id = any(), copyPriority = false, copyScheduler = false) } answers {
            stubTask(id = secondArg<Int>().toInt())
        }
        every { service.updateTask(any(), any(), any()) } returns stubTask()
    }

    @Test
    fun `run creation if scheduled for one shot`() = runTest {
        CurrentTimeUtil.setOtherTime(16, 5, 2021, 14, 30)
        val dispatcher = schedulerDispatcher(scheduledOneShotTasks)

        dispatcher.dispatch()

        verify {
            service.copyTask(user.id, 1, copyPriority = false, copyScheduler = false)
            fcmService.sendPushNotification(user.id, any())
        }
    }

    @Test
    fun `do not create one shot if scheduled too early`() = runTest {
        CurrentTimeUtil.setOtherTime(16, 5, 2021, 14, 36)
        val dispatcher = schedulerDispatcher(scheduledOneShotTasks)

        dispatcher.dispatch()

        verify(inverse = true) { service.copyTask(any(), any()) }
    }

    @Test
    fun `do not create one shot if scheduled too late`() = runTest {
        CurrentTimeUtil.setOtherTime(16, 5, 2021, 14, 19)
        val dispatcher = schedulerDispatcher(scheduledOneShotTasks)

        dispatcher.dispatch()

        verify(inverse = true) { service.copyTask(any(), any()) }
    }

    @Test
    fun `run creation if scheduled monthly`() = runTest {
        CurrentTimeUtil.setOtherTime(20, 5, 2021, 14, 30)
        val dispatcher = schedulerDispatcher(monthlyScheduler)

        dispatcher.dispatch()

        verifyCreated()
    }

    @Test
    fun `run creation if scheduled monthly with creation time`() = runTest {
        verifyDispatcher(
            monthlyScheduler.copy(
                creationDate = LocalDateTime.of(2021, 5, 16, 15, 0).toKotlinLocalDateTime(),
                dayOfMonth = 16
            ),
            day = 16,
            month = 10,
            year = 2021,
            hour = 14,
            minute = 30
        )
    }

    @Test
    fun `run creation if scheduled monthly for last date`() = runTest {
        // In november is winter time, one hour backward
        CurrentTimeUtil.setOtherTime(30, 11, 2021, 15, 30)
        val dispatcher =
            schedulerDispatcher(
                mapOf(
                    user to listOf(
                        stubTask(
                            id = 1,
                            description = "1",
                            subTasks = listOf(subTask),
                            scheduler = monthlyScheduler
                        ).copy(scheduler = monthlyScheduler.copy(repeatCount = 4, dayOfMonth = 30))
                    )
                )
            )

        dispatcher.dispatch()

        verifyCreated()
    }

    @Test
    fun `run creation if scheduled monthly for last date - case 2`() = runTest {
        // In november is winter time, one hour backward
        CurrentTimeUtil.setOtherTime(9, 8, 2022, 8, 13)
        val dispatcher =
            schedulerDispatcher(
                mapOf(
                    user to listOf(
                        stubTask(
                            id = 1,
                            description = "1",
                            subTasks = listOf(subTask),
                            scheduler = monthlyScheduler
                        ).copy(
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
            )

        dispatcher.dispatch()

        verify(exactly = 0) {
            service.copyTask(userId = user.id, id = 1, copyPriority = false, copyScheduler = false)
        }
    }

    @Test
    fun `do not create if scheduled monthly and month not in step`() = runTest {
        CurrentTimeUtil.setOtherTime(20, 6, 2021, 14, 30)
        val dispatcher = schedulerDispatcher(monthlyScheduler)

        dispatcher.dispatch()

        verify(inverse = true) {
            service.copyTask(userId = user.id, id = 1, copyPriority = false, copyScheduler = false)
        }
    }

    @Test
    fun `do not create if scheduled monthly and wrong day, but good hour`() = runTest {
        CurrentTimeUtil.setOtherTime(21, 5, 2021, 14, 30)
        val dispatcher = schedulerDispatcher(monthlyScheduler)

        dispatcher.dispatch()

        verify(inverse = true) {
            service.copyTask(userId = user.id, id = 1, copyPriority = false, copyScheduler = false)
        }
    }

    @Test
    fun `do not create if scheduled monthly and not proper time`() = runTest {
        CurrentTimeUtil.setOtherTime(21, 6, 2021, 14, 30)
        val scheduler = monthlyScheduler
        val dispatcher = schedulerDispatcher(scheduler)

        dispatcher.dispatch()

        verify(inverse = true) {
            service.copyTask(userId = user.id, id = 1, copyPriority = false, copyScheduler = false)
        }
    }

    @Test
    fun `do not create if scheduled monthly and wrong hour`() = runTest {
        CurrentTimeUtil.setOtherTime(20, 6, 2021, 14, 55)
        val dispatcher = schedulerDispatcher(monthlyScheduler)

        dispatcher.dispatch()

        verify(inverse = true) {
            service.copyTask(userId = user.id, id = 1, copyPriority = false, copyScheduler = false)
        }
    }

    @Test
    fun `create if scheduled weekly`() = runTest {
        CurrentTimeUtil.setOtherTime(19, 5, 2021, 14, 30)
        val dispatcher = schedulerDispatcher(scheduledWeeklyTasks)

        dispatcher.dispatch()

        verifyCreated()
    }

    @Test
    fun `if creation time higher than start date, begin from next period, success case`() = runTest {
        CurrentTimeUtil.setOtherTime(20, 6, 2021, 14, 30)
        val dispatcher = schedulerDispatcher(weeklyScheduler.copy(
            creationDate = LocalDateTime.of(2021, 5, 16, 17, 0,0).toKotlinLocalDateTime(),
            daysOfWeek = listOf(DayOfWeek.SUNDAY)
        ))

        dispatcher.dispatch()

        verifyCreated()
    }

    private fun verifyNotCreated() {
        verify(inverse = true) {
            service.copyTask(any(), any(), any())
        }
    }

    @Test
    fun `create if scheduled weekly and today is in 2 week from start date`() = runTest {
        CurrentTimeUtil.setOtherTime(2, 6, 2021, 14, 30)
        val dispatcher = schedulerDispatcher(scheduledWeeklyTasks)

        dispatcher.dispatch()

        verifyCreated()
    }

    @Test
    fun `create if scheduled weekly per 2 weeks and today is in 4 week from start date`() = runTest {
        CurrentTimeUtil.setOtherTime(18, 6, 2021, 14, 30)
        val dispatcher = schedulerDispatcher(scheduledWeeklyTasksWithLastDay)

        dispatcher.dispatch()

        verifyCreated()
    }

    private fun verifyCreated() {
        verify {
            service.copyTask(userId = user.id, id = 1, copyPriority = false, copyScheduler = false)
            fcmService.sendPushNotification(user.id, any())
        }
    }

    @Test
    fun `do not create if scheduled weekly but wrong hour`() = runTest {
        verifyDispatcher(scheduledWeeklyTasks, 19, 5, 2021, 14, 55, true)
    }


    @Test
    fun `do not create if scheduled weekly but day out of range`() = runTest {
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
    fun `create if scheduled weekly, 30 may case`() = runTest {
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
    fun `create if scheduled weekly, 27 may case`() = runTest {
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
    fun `create if scheduled weekly, 31 may case`() = runTest {
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

    private suspend fun verifyDispatcher(
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
            fcmService.sendPushNotification(user.id, any())
        }
    }

    private suspend fun verifyDispatcher(
        scheduler: Scheduler,
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
            fcmService.sendPushNotification(user.id, any())
        }
    }


    private fun schedulerDispatcher(scheduledTasks: Map<User, List<Task>>): SchedulerDispatcher = SchedulerDispatcher(
        todoRepository = repository.apply {
            every { getScheduledTasks() } returns scheduledTasks
        },
        todoService = service,
        schedulerDispatcherInterval = 15 * SECONDS_IN_MINUTE * MILLISECONDS_IN_SECOND,
        timeZoneOffsetHours = 2,
        eventBus = EventBus(),
        fcmService = fcmService
    )

    private fun schedulerDispatcher(scheduler: Scheduler) = schedulerDispatcher(
        mapOf(
            user to listOf(
                stubTask(
                    id = 1,
                    description = "1",
                    subTasks = listOf(subTask),
                    scheduler = scheduler
                )
            )
        )
    )

}