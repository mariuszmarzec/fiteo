package com.marzec.todo.api

import com.marzec.todo.model.ToDoList
import com.marzec.todo.repositories.TodoRepository

class TodoService(
        private val repository: TodoRepository
) {
    fun getLists(userId: Int): List<ToDoList> {
        return repository.getLists(userId)
    }

    fun addList(userId: Int, listName: String): ToDoList {
        return repository.addList(userId, listName)
    }

    fun removeList(userId: Int, listId: Int): ToDoList {
        return repository.removeList(userId, listId)
    }
}
