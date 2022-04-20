package com.marzec.todo

import com.marzec.todo.model.CreateTask
import com.marzec.todo.model.Task
import com.marzec.todo.model.UpdateTask

class TodoService(
        private val repository: TodoRepository
) {
    fun getTasks(userId: Int): List<Task> = repository.getTasks(userId)

    fun addTask(userId: Int, task: CreateTask): Task = repository.addTask(userId, task)

    fun updateTask(userId: Int, taskId: Int, task: UpdateTask): Task = repository.updateTask(userId, taskId, task)

    fun removeTask(userId: Int, taskId: Int): Task = repository.removeTask(userId, taskId)

    fun markAsToDo(userId: Int, isToDo: Boolean, taskIds: List<Int>): List<Task> =
        repository.markAsToDo(userId, isToDo, taskIds).run { repository.getTasks(userId) }
}
