package com.marzec.todo.schedule

import com.marzec.core.currentTime
import com.marzec.di.Di
import com.marzec.di.MILLISECONDS_IN_SECOND
import com.marzec.fiteo.model.domain.NullableField
import com.marzec.todo.TodoRepository
import com.marzec.todo.TodoService
import com.marzec.todo.model.Scheduler
import com.marzec.todo.model.Task
import com.marzec.todo.model.UpdateTask
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime
import org.slf4j.LoggerFactory
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.time.YearMonth

class SchedulerDispatcher(
    private val todoRepository: TodoRepository,
    private val todoService: TodoService,
    private val schedulerDispatcherInterval: Long,
    private val timeZoneOffsetHours: Long
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    private val creationTimeFeatureEnabled = true

    fun dispatch() {
        todoRepository.getScheduledTasks().forEach { (user, tasks) ->
            tasks.forEach { task ->
                if (task.scheduler?.shouldBeCreated() == true) {
                    todoService.copyTask(
                        userId = user.id,
                        id = task.id,
                        copyPriority = false,
                        copyScheduler = false,
                        highestPriorityAsDefault = task.scheduler.highestPriorityAsDefault
                    )
                    if ((task.scheduler as? Scheduler.OneShot)?.removeScheduled == true) {
                        todoService.removeTask(
                            userId = user.id,
                            taskId = task.id,
                            removeWithSubtasks = true
                        )
                    } else {
                        updateLastDate(user.id, task)
                    }
                } else {
                    logger.debug("TASK ${task.id} not added by scheduler")
                }
            }
        }
    }

    private fun Scheduler.shouldBeCreated(): Boolean {
        return when (this) {
            is Scheduler.OneShot -> shouldBeCreated()
            is Scheduler.Monthly -> shouldBeCreated()
            is Scheduler.Weekly -> shouldBeCreated()
        }
    }

    private fun Scheduler.OneShot.shouldBeCreated(): Boolean {
        val creationTime = startDate.toJavaLocalDateTime()
            .withHour(hour)
            .withMinute(minute)
        return lastDate == null && isInStartWindow(creationTime)
    }

    private fun Scheduler.Monthly.shouldBeCreated(): Boolean {
        val today = currentTime().toJavaLocalDateTime()
        if (today.targetDayOfMonth(dayOfMonth) != today.dayOfMonth) {
            return false
        }

        return shouldCreate(
            calcFirstPeriodDate = { startDate: LocalDate ->
                val realDayOfMonth =
                    if (dayOfMonth > startDate.lastDayOfTheMonth()) startDate.lastDayOfTheMonth() else dayOfMonth
                val startedInNextMonth = startDate.dayOfMonth > realDayOfMonth
                startDate.withDayOfMonth(realDayOfMonth)
                    .let { if (startedInNextMonth) it.plusMonths(1) else it }
            },
            calcPeriodNumber = { firstPeriodDate, todayLocalDate ->
                Period.between(firstPeriodDate.plusDays(-1), todayLocalDate).months.inc()
            }
        )
    }

    private fun Scheduler.Weekly.shouldBeCreated(): Boolean {
        val today = currentTime().toJavaLocalDateTime()
        if (daysOfWeek.isNotEmpty() && today.dayOfWeek !in daysOfWeek) {
            return false
        }

        return shouldCreate(
            calcFirstPeriodDate = { startDate: LocalDate ->
                startDate.findFirstDate { it.dayOfWeek in daysOfWeek }
                    ?.findFirstDate(
                        mutate = { it.plusDays(-1) },
                        predicate = { it.dayOfWeek == DayOfWeek.MONDAY }
                    )
            },
            calcPeriodNumber = { firstPeriodDate, todayLocalDate ->
                val firstPeriodDayOfToday = todayLocalDate.findFirstDate(
                    mutate = { it.plusDays(-1) },
                    predicate = { it.dayOfWeek == DayOfWeek.MONDAY }
                )
                println("firstPeriodDate $firstPeriodDate")
                println("firstPeriodDayOfToday $firstPeriodDayOfToday")
                val daysBetween = Period.between(firstPeriodDate, firstPeriodDayOfToday).days
                daysBetween / WEEK_DAYS_COUNT + 1
            }
        )
    }

    private fun Scheduler.shouldCreate(
        calcFirstPeriodDate: (startDate: LocalDate) -> LocalDate?,
        calcPeriodNumber: (firstPeriodDate: LocalDate, todayLocalDate: LocalDate) -> Int
    ): Boolean {
        val today = currentTime().toJavaLocalDateTime()
        val creationDate = creationDate?.toJavaLocalDateTime()?.takeIf { creationTimeFeatureEnabled } ?: LocalDateTime.MIN
        val startDateWithHour = startDate.toJavaLocalDateTime().withHour(hour).withMinute(minute)
        val startDate = if (creationDate > startDateWithHour) {
            if (creationDate.toLocalDate() == startDateWithHour.toLocalDate()) {
                creationDate.plusDays(1)
            } else {
                creationDate
            }
        } else {
            startDateWithHour
        }.toLocalDate()
        val todayLocalDate = today.toLocalDate()

        val firstPeriodDate = calcFirstPeriodDate(startDate) ?: return false

        if (firstPeriodDate <= todayLocalDate) {
            val periodNumber = calcPeriodNumber(firstPeriodDate, todayLocalDate)

            val isRightPeriod = (periodNumber - 1).mod(repeatInEveryPeriod) == 0
            val isInCountLimit = repeatCount.takeIf { it > 0 }?.let { maxCount ->
                (periodNumber - 1) / repeatInEveryPeriod.toFloat() + 1 <= maxCount
            } ?: true
            println("periodsBetween $periodNumber")
            println("repeatInEveryPeriod $repeatInEveryPeriod")
            println("isRightPeriod $isRightPeriod")
            println("isInCountLimit $isInCountLimit")

            if (isRightPeriod && isInCountLimit) {
                val creationTime = today.withHour(hour).withMinute(minute)
                return isInStartWindow(creationTime)
            }
        }
        return false
    }

    private fun LocalDate.findFirstDate(
        maxDate: LocalDate = currentTime().toJavaLocalDateTime().toLocalDate().plusMonths(3),
        mutate: (LocalDate) -> LocalDate = { it.plusDays(1) },
        predicate: (LocalDate) -> Boolean
    ): LocalDate? =
        if (predicate(this)) {
            this
        } else {
            val nextDate = mutate(this)
            if (nextDate <= maxDate) {
                nextDate.findFirstDate(maxDate, mutate, predicate)
            } else {
                null
            }
        }

    private fun isInStartWindow(creationTime: LocalDateTime): Boolean {
        val normalisedCreationTime = creationTime.minusHours(timeZoneOffsetHours)

        val intervalEndTime = currentTime().toJavaLocalDateTime()
        val intervalStartTime = intervalEndTime.minusSeconds(schedulerDispatcherInterval / MILLISECONDS_IN_SECOND)

        return intervalStartTime <= normalisedCreationTime && normalisedCreationTime <= intervalEndTime
    }


    private fun updateLastDate(userId: Int, task: Task) {
        todoService.updateTask(userId, task.id, task.toUpdateWithCurrentLastDate(timeZoneOffsetHours))
    }

    companion object {
        private const val WEEK_DAYS_COUNT = 7
    }
}

private fun Task.toUpdateWithCurrentLastDate(timeZoneOffsetHours: Long): UpdateTask {
    val lastDate = currentTime().toJavaLocalDateTime()
        .plusHours(timeZoneOffsetHours)
        .toKotlinLocalDateTime()
    return UpdateTask(
        scheduler = NullableField(scheduler?.updateLastDate(lastDate)),
    )
}

fun runTodoSchedulerDispatcher(vararg dis: Di) {
    dis.forEach { di ->
        val schedulerDispatcher = di.schedulerDispatcher
        GlobalScope.launch {
            while (true) {
                schedulerDispatcher.dispatch()
                delay(di.schedulerDispatcherInterval)
            }
        }
    }
}

private fun LocalDate.lastDayOfTheMonth() = YearMonth.of(year, month).atEndOfMonth().dayOfMonth

private fun LocalDateTime.targetDayOfMonth(targetDayOfMonth: Int): Int {
    val lastDayOfTheMonth = this.toLocalDate().lastDayOfTheMonth()
    return if (targetDayOfMonth > lastDayOfTheMonth) lastDayOfTheMonth else targetDayOfMonth
}