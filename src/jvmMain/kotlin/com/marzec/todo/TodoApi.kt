package com.marzec.todo

import com.marzec.common.deleteByIdEndpoint
import com.marzec.common.getByIdEndpoint
import com.marzec.common.postEndpoint
import com.marzec.common.updateByIdEndpoint
import com.marzec.di.Di
import io.ktor.auth.authenticate
import io.ktor.routing.Route

fun Route.todoApi(di: Di, todoController: ToDoApiController) {
    authenticate(di.authToken) {
        todoLists(todoController)
        addTodoList(todoController)
        deleteTodoList(todoController)

        addTask(todoController)
        updateTask(todoController)
        removeTask(todoController)

    }
}

fun Route.todoLists(api: ToDoApiController) = getByIdEndpoint(ApiPath.TODO_LISTS, api::getLists)

fun Route.addTodoList(api: ToDoApiController) = postEndpoint(ApiPath.TODO_LIST, api::addList)

fun Route.deleteTodoList(api: ToDoApiController) = deleteByIdEndpoint(ApiPath.DELETE_TODO_LIST, api::removeList)

fun Route.addTask(api: ToDoApiController) = postEndpoint(ApiPath.ADD_TASK, api::addTask)

fun Route.updateTask(api: ToDoApiController) = updateByIdEndpoint(ApiPath.UPDATE_TASK, api::updateTask)

fun Route.removeTask(api: ToDoApiController) = deleteByIdEndpoint(ApiPath.DELETE_TASK, api::removeTask)
