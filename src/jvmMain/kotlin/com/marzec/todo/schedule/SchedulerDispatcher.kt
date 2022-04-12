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

class SchedulerDispatcher(
    private val todoRepository: TodoRepository,
    private val schedulerDispatcherInterval: Long,
    private val timeZoneOffsetHours: Long
) {
    fun dispatch() {
        todoRepository.getScheduledTasks().forEach { (user, tasks) ->
            tasks.forEach { task ->
                if (task.scheduler?.shouldBeCreated(schedulerDispatcherInterval) == true) {
                    createTaskCopy(user.id, task)
                    updateLastDate(user.id, task)
                }
            }
        }
    }

    private fun Scheduler.shouldBeCreated(schedulerDispatcherInterval: Long): Boolean {
        val intervalEndTime = currentTime().toJavaLocalDateTime()
        val intervalStartTime = intervalEndTime.minusSeconds(schedulerDispatcherInterval / MILLISECONDS_IN_SECOND)
        return when (this) {
            is Scheduler.OneShot -> shouldBeCreated()
            is Scheduler.Monthly -> {
                val dayOfMonth = if (dayOfMonth > 27) currentTime().month.maxLength() else dayOfMonth
                val startedInNextMonth = startDate.dayOfMonth < dayOfMonth
                val maxDate = repeatInEveryPeriod.timesIf(repeatCount) {
                    repeatCount > 0
                }.incIf { startedInNextMonth }
                val creationTime = if (lastDate != null) {
                    lastDate!!.toJavaLocalDateTime()
                        .withHour(hour)
                        .withMinute(minute)
                        .withDayOfMonth(dayOfMonth)
                } else {
                    startDate
                        .toJavaLocalDateTime()
                        .withHour(hour)
                        .withMinute(minute)
                        .plusMonths(if (startedInNextMonth) 1 else 0)
                        .withDayOfMonth(dayOfMonth)
                }
                startDate.toJavaLocalDateTime() <= creationTime && intervalStartTime <= creationTime && creationTime <= intervalEndTime
            }
            is Scheduler.Weekly -> {
                startDate.toJavaLocalDateTime().withHour(hour).withMinute(minute)
                false
            }
        }
    }

    private fun Scheduler.OneShot.shouldBeCreated(): Boolean {
        val creationTime = startDate.toJavaLocalDateTime()
            .withHour(hour)
            .minusHours(timeZoneOffsetHours)
            .withMinute(minute)
        return lastDate == null && isInStartWindow(creationTime)
    }

    private fun isInStartWindow(creationTime: LocalDateTime): Boolean {
        val intervalEndTime = currentTime().toJavaLocalDateTime()
        val intervalStartTime = intervalEndTime.minusSeconds(schedulerDispatcherInterval / MILLISECONDS_IN_SECOND)

        println("intervalEndTime $intervalEndTime")
        println("intervalStartTime $intervalStartTime")
        println("creationTime $creationTime")
        return intervalStartTime <= creationTime && creationTime <= intervalEndTime
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