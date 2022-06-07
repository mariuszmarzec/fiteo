package com.marzec.todo

import com.marzec.todo.model.CreateTask
import com.marzec.todo.model.Task
import com.marzec.todo.model.UpdateTask

class TodoService(
    private val repository: TodoRepository
) {
    fun getTasks(userId: Int): List<Task> = repository.getTasks(userId)

    fun addTask(userId: Int, task: CreateTask): Task = repository.addTask(userId, task)

    fun copyTask(
        userId: Int,
        id: Int,
        copyPriority: Boolean = true,
        copyScheduler: Boolean = true,
        highestPriorityAsDefault: Boolean = false,
    ): Task {
        val taskToCopy = repository.getTask(userId, id)
        val copiedTaskId = createTaskCopy(
            userId = userId,
            task = taskToCopy,
            parentTaskId = taskToCopy.parentTaskId,
            copyPriority = copyPriority,
            copyScheduler = copyScheduler,
            highestPriorityAsDefault = highestPriorityAsDefault
        )
        return repository.getTask(userId, copiedTaskId)
    }

    fun updateTask(userId: Int, taskId: Int, task: UpdateTask): Task = repository.updateTask(userId, taskId, task)

    fun removeTask(userId: Int, taskId: Int): Task = repository.removeTask(userId, taskId)

    fun markAsToDo(userId: Int, isToDo: Boolean, taskIds: List<Int>): List<Task> =
        repository.markAsToDo(userId, isToDo, taskIds).run { repository.getTasks(userId) }

    private fun createTaskCopy(
        userId: Int,
        task: Task,
        parentTaskId: Int? = null,
        copyPriority: Boolean,
        copyScheduler: Boolean,
        highestPriorityAsDefault: Boolean,
    ): Int {
        val newTask = repository.addTask(
            userId,
            task.toCreateTask(parentTaskId, copyPriority, copyScheduler, highestPriorityAsDefault)
        )
        task.subTasks.forEach { subTask ->
            createTaskCopy(userId, subTask, newTask.id, copyPriority, copyScheduler, highestPriorityAsDefault = false)
        }
        return newTask.id
    }

    private fun Task.toCreateTask(
        parentTaskId: Int? = null,
        copyPriority: Boolean,
        copyScheduler: Boolean,
        highestPriorityAsDefault: Boolean
    ) = CreateTask(
        description = description,
        parentTaskId = parentTaskId,
        priority = priority.takeIf { copyPriority },
        highestPriorityAsDefault = highestPriorityAsDefault,
        scheduler = scheduler.takeIf { copyScheduler }
    )
}
