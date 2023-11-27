package com.marzec.todo

import com.marzec.fiteo.model.domain.User
import com.marzec.todo.model.CreateTask
import com.marzec.todo.model.Task
import com.marzec.todo.model.UpdateTask
import com.marzec.todo.model.UpdateTask2

interface TodoRepository {

    fun getTasks(userId: Int): List<Task>

    fun getTask(userId: Int, id: Int): Task

    fun getScheduledTasks(): Map<User, List<Task>>

    fun addTask(userId: Int, task: CreateTask): Task

    fun updateTask(userId: Int, taskId: Int, task: UpdateTask): Task

    fun updateTask(userId: Int, taskId: Int, task: UpdateTask2): Task

    fun removeTask(userId: Int, taskId: Int, removeWithSubtasks: Boolean): Task

    fun markAsToDo(userId: Int, isToDo: Boolean, taskIds: List<Int>)
}
