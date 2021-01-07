package com.marzec.todo.repositories

import com.marzec.todo.model.CreateTask
import com.marzec.todo.model.Task
import com.marzec.todo.model.ToDoList

interface TodoRepository {

    fun getLists(userId: Int): List<ToDoList>

    fun addList(userId: Int, listName: String): ToDoList

    fun removeList(userId: Int, listId: Int): ToDoList

    fun addTask(userId: Int, listId: Int, task: CreateTask): Task
}