package com.marzec.todo.repositories

import com.marzec.todo.model.ToDoList

interface TodoRepository {

    fun getLists(userId: Int): List<ToDoList>
}