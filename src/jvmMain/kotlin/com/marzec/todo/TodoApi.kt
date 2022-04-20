package com.marzec.todo

import com.marzec.common.deleteByIdEndpoint
import com.marzec.common.getAllEndpoint
import com.marzec.common.postEndpoint
import com.marzec.common.updateByIdEndpoint
import com.marzec.di.Di
import io.ktor.auth.*
import io.ktor.routing.*

fun Route.todoApi(di: Di, todoController: ToDoApiController) {
    authenticate(di.authToken) {
        tasks(todoController)
        markAsToDo(todoController)
        addTask(todoController)
        updateTask(todoController)
        removeTask(todoController)
    }
}

fun Route.updateTask(api: ToDoApiController) = updateByIdEndpoint(ApiPath.UPDATE_TASK, api::updateTask)

fun Route.removeTask(api: ToDoApiController) = deleteByIdEndpoint(ApiPath.DELETE_TASK, api::removeTask)

fun Route.tasks(api: ToDoApiController) = getAllEndpoint(ApiPath.TASKS, api::getTasks)

fun Route.addTask(api: ToDoApiController) = postEndpoint(ApiPath.ADD_TASK, api::addTask)

fun Route.markAsToDo(api: ToDoApiController) = postEndpoint(ApiPath.MARK_AS_TO_DO, api::markAsToDo)
