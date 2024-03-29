package com.marzec.todo

import com.marzec.common.*
import com.marzec.di.Di
import io.ktor.server.auth.*
import io.ktor.server.routing.Route

fun Route.todoApi(di: Di, todoController: ToDoApiController) {
    authenticate(di.authToken) {
        tasks(todoController)
        copyTask(todoController)
        markAsToDo(todoController)
        addTask(todoController)
        updateTask(todoController)
        removeTask(todoController)
    }
}

fun Route.updateTask(api: ToDoApiController) = updateByIdEndpoint(
    path = ApiPath.UPDATE_TASK,
    apiFunRef = api::updateTask
)

fun Route.removeTask(api: ToDoApiController) = deleteByIdEndpoint(ApiPath.DELETE_TASK, api::removeTask)

fun Route.tasks(api: ToDoApiController) = getAllEndpoint(ApiPath.TASKS, api::getTasks)

fun Route.copyTask(api: ToDoApiController) = getByIdEndpoint(ApiPath.COPY_TASK, api::copyTasks)

fun Route.addTask(api: ToDoApiController) = postEndpoint(ApiPath.ADD_TASK, api::addTask)

fun Route.markAsToDo(api: ToDoApiController) = postEndpoint(ApiPath.MARK_AS_TO_DO, api::markAsToDo)
