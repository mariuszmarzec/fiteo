package com.marzec.todo.schedule

import com.marzec.core.currentMillis
import com.marzec.core.currentTime
import com.marzec.di.Di
import com.marzec.todo.TodoRepository
import com.marzec.todo.TodoService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.toJavaLocalDateTime
import org.slf4j.LoggerFactory

class TaskExpirationDispatcher(
    private val todoRepository: TodoRepository,
    private val todoService: TodoService
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO.limitedParallelism(100))

    init {
        logger.info("Task expiration dispatcher started")
    }

    fun dispatch() {
        val today = currentTime().toJavaLocalDateTime()
        todoRepository.getTasksWithExpirationDate().forEach { (user, tasks) ->
            tasks.forEach { task ->
                coroutineScope.launch {
                    val expirationDate = task.expirationDate?.toJavaLocalDateTime()
                    if (expirationDate != null && expirationDate.isBefore(today)) {
                        todoService.removeTask(
                            userId = user.id,
                            taskId = task.id,
                            removeWithSubtasks = true
                        )
                        logger.info("Removed expired task: \${task.id} for user: \${user.id}")
                    }
                }
            }
        }
    }
}

fun runTaskExpirationDispatcher(scope: CoroutineScope, vararg dis: Di) {
    dis.forEach { di ->
        val expirationDispatcher = di.taskExpirationDispatcher
        val dispatcherInterval = di.schedulerDispatcherInterval
        scope.launch {
            while (true) {
                val startTime = currentMillis()
                expirationDispatcher.dispatch()
                val endTime = currentMillis()
                val duration = endTime - startTime
                if (duration < dispatcherInterval) {
                    delay(dispatcherInterval - duration)
                }
            }
        }
    }
}
