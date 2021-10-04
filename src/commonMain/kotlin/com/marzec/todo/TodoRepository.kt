package com.marzec.todo

import com.marzec.todo.model.CreateTask
import com.marzec.todo.model.Task
import com.marzec.todo.model.ToDoList
import com.marzec.todo.model.UpdateTask

interface TodoRepository {

    fun getLists(userId: Int): List<ToDoList>

    fun addList(userId: Int, listName: String): ToDoList

    fun removeList(userId: Int, listId: Int): ToDoList

    fun addTask(userId: Int, listId: Int, task: CreateTask): Task

    fun updateTask(userId: Int, taskId: Int, task: UpdateTask): Task

    fun removeTask(userId: Int, taskId: Int): Task
}
