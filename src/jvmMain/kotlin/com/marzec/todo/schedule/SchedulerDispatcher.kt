package com.marzec.todo.schedule

import com.marzec.core.currentTime
import com.marzec.di.Di
import com.marzec.di.MILLISECONDS_IN_SECOND
import com.marzec.todo.TodoRepository
import com.marzec.todo.model.CreateTask
import com.marzec.todo.model.Scheduler
import com.marzec.todo.model.Task
import com.marzec.todo.model.UpdateTask
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime
import java.time.LocalDateTime
import java.time.Period

class SchedulerDispatcher(
    private val todoRepository: TodoRepository,
    private val schedulerDispatcherInterval: Long,
    private val timeZoneOffsetHours: Long
) {
    fun dispatch() {
        todoRepository.getScheduledTasks().forEach { (user, tasks) ->
            tasks.forEach { task ->
                if (task.scheduler?.shouldBeCreated() == true) {
                    createTaskCopy(user.id, task)
                    updateLastDate(user.id, task)
                }
            }
        }
    }

    private fun Scheduler.shouldBeCreated(): Boolean {
        return when (this) {
            is Scheduler.OneShot -> shouldBeCreated()
            is Scheduler.Monthly -> shouldBeCreated()
            is Scheduler.Weekly -> {
                false
            }
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
        val dayOfMonth = if (dayOfMonth > 27) 28 else dayOfMonth
        val startedInNextMonth = startDate.dayOfMonth >= dayOfMonth
        val firstDate = startDate.toJavaLocalDateTime()
            .let { if (startedInNextMonth) it.plusMonths(1L) else it }
            .withHour(hour)
            .withMinute(minute)
            .withDayOfMonth(dayOfMonth)
        val maxDate = repeatCount.takeIf { it > 0 }
            ?.let { firstDate.plusMonths(it * repeatInEveryPeriod.toLong()) }
        val firstDateLocal = firstDate.toLocalDate()
        val todayLocalDate = today.toLocalDate()

        if (firstDateLocal <= todayLocalDate && (maxDate?.let { today <= it } != false)) {
            if (Period.between(firstDateLocal, todayLocalDate).months % repeatInEveryPeriod == 0) {
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

        println("intervalEndTime $intervalEndTime")
        println("intervalStartTime $intervalStartTime")
        println("creationTime $normalisedCreationTime")
        return intervalStartTime <= normalisedCreationTime && normalisedCreationTime <= intervalEndTime
    }

    private fun createTaskCopy(userId: Int, task: Task, parentTaskId: Int? = null) {
        val newTask = todoRepository.addTask(userId, task.toCreateTask(parentTaskId))
        task.subTasks.forEach { subTask ->
            createTaskCopy(userId, subTask, newTask.id)
        }
    }

    private fun updateLastDate(userId: Int, task: Task) {
        todoRepository.updateTask(userId, task.id, task.toUpdateWithCurrentLastDate(timeZoneOffsetHours))
    }
}

private fun Task.toCreateTask(parentTaskId: Int? = null) = CreateTask(
    description = description,
    parentTaskId = parentTaskId,
    priority = null,
    highestPriorityAsDefault = false,
    scheduler = null
)

private fun Task.toUpdateWithCurrentLastDate(timeZoneOffsetHours: Long): UpdateTask {
    val lastDate = currentTime().toJavaLocalDateTime()
        .plusHours(timeZoneOffsetHours)
        .toKotlinLocalDateTime()
    return UpdateTask(
        description = description,
        parentTaskId = parentTaskId,
        priority = priority,
        isToDo = isToDo,
        scheduler = scheduler?.updateLastDate(lastDate),
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

private fun Int.timesIf(other: Int, condition: () -> Boolean) = if (condition()) this.times(other) else this

private fun Int.incIf(condition: () -> Boolean) = if (condition()) inc() else this