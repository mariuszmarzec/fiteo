package com.marzec.todo

import com.marzec.todo.model.CreateTask
import com.marzec.todo.model.Task
import com.marzec.todo.model.ToDoList
import com.marzec.todo.model.UpdateTask
import com.marzec.todo.TodoRepository

class TodoService(
        private val repository: TodoRepository
) {
    fun getTasks(userId: Int): List<Task> {
        return repository.getLists(userId).first().tasks
    }

    fun getLists(userId: Int): List<ToDoList> {
        return repository.getLists(userId)
    }

    fun addList(userId: Int, listName: String): ToDoList {
        return repository.addList(userId, listName)
    }

    fun removeList(userId: Int, listId: Int): ToDoList {
        return repository.removeList(userId, listId)
    }

    fun addTask(userId: Int, listId: Int, task: CreateTask): Task {
        return repository.addTask(userId, listId, task)
    }

    fun updateTask(userId: Int, taskId: Int, task: UpdateTask): Task {
        return repository.updateTask(userId, taskId, task)
    }

    fun removeTask(userId: Int, taskId: Int): Task {
        return repository.removeTask(userId, taskId)
    }
}
