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
import java.time.LocalDateTime
import java.time.Period
import java.time.YearMonth
import kotlin.math.ceil

class SchedulerDispatcher(
    private val todoRepository: TodoRepository,
    private val todoService: TodoService,
    private val schedulerDispatcherInterval: Long,
    private val timeZoneOffsetHours: Long
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

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
        val dayOfMonth = if (dayOfMonth > 27) today.lastDayOfTheMonth() else dayOfMonth
        val startedInNextMonth = startDate.dayOfMonth >= dayOfMonth
        val firstDate = startDate.toJavaLocalDateTime()
            .let { if (startedInNextMonth) it.plusMonths(1L) else it }
            .withHour(hour)
            .withMinute(minute)
            .let {
                it.withDayOfMonth(if (dayOfMonth > 27) it.lastDayOfTheMonth() else dayOfMonth)
            }
        val maxDate = repeatCount.takeIf { it > 0 }
            ?.let { firstDate.plusMonths(it * repeatInEveryPeriod.toLong()) }
        val firstDateLocal = firstDate.toLocalDate()
        val todayLocalDate = today.toLocalDate()

        if (firstDateLocal <= todayLocalDate && (maxDate?.let { today <= it } != false)) {
            if (Period.between(firstDateLocal, todayLocalDate.plusDays(1)).months % repeatInEveryPeriod == 0) {
                val creationTime = today.withHour(hour).withMinute(minute).withDayOfMonth(dayOfMonth)
                return isInStartWindow(creationTime)
            }
        }
        return false
    }

    private fun Scheduler.Weekly.shouldBeCreated(): Boolean {
        val today = currentTime().toJavaLocalDateTime()
        val firstDate = startDate.toJavaLocalDateTime()
            .withHour(hour)
            .withMinute(minute)
        val maxDate = repeatCount.takeIf { it > 0 }
            ?.let { firstDate.plusDays(it.dec() * WEEK_DAYS_COUNT * repeatInEveryPeriod.toLong()) }
        val firstDateLocal = firstDate.toLocalDate()
        val todayLocalDate = today.toLocalDate()

        if (daysOfWeek.isNotEmpty() && today.dayOfWeek !in daysOfWeek) {
            return false
        }
        val isLessOrEqualMaxDate = maxDate?.let { today <= it } != false
        if (firstDateLocal <= todayLocalDate && isLessOrEqualMaxDate) {
            if (ceil(Period.between(firstDateLocal, todayLocalDate).days / WEEK_DAYS_COUNT.toFloat()).toInt().dec()
                    .mod(repeatInEveryPeriod) == 0
            ) {
                val creationTime = today.withHour(hour).withMinute(minute)
                return isInStartWindow(creationTime)
            }
        }
        return false
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

private fun LocalDateTime.lastDayOfTheMonth() = YearMonth.of(year, month).atEndOfMonth().dayOfMonth