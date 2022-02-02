package com.marzec.todo

import com.marzec.todo.model.CreateTask
import com.marzec.todo.model.Task
import com.marzec.todo.model.UpdateTask

interface TodoRepository {

    fun getTasks(userId: Int): List<Task>

    fun addTask(userId: Int, task: CreateTask): Task

    fun updateTask(userId: Int, taskId: Int, task: UpdateTask): Task

    fun removeTask(userId: Int, taskId: Int): Task
}
