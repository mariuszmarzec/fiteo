package com.marzec.todo

import com.marzec.common.deleteByIdEndpoint
import com.marzec.common.getAllEndpoint
import com.marzec.common.getByIdEndpoint
import com.marzec.common.postEndpoint
import com.marzec.common.updateByIdEndpoint
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
        removeTaskWithSubtasks(todoController)
    }
}

fun Route.updateTask(api: ToDoApiController) = updateByIdEndpoint(ApiPath.UPDATE_TASK, api::updateTask)

fun Route.removeTask(api: ToDoApiController) = deleteByIdEndpoint(ApiPath.DELETE_TASK, api::removeTask)

fun Route.removeTaskWithSubtasks(api: ToDoApiController) = postEndpoint(ApiPath.DELETE_TASK_WITH_SUBTASKS, api::removeTaskWithSubtask)

fun Route.tasks(api: ToDoApiController) = getAllEndpoint(ApiPath.TASKS, api::getTasks)

fun Route.copyTask(api: ToDoApiController) = getByIdEndpoint(ApiPath.COPY_TASK, api::copyTasks)

fun Route.addTask(api: ToDoApiController) = postEndpoint(ApiPath.ADD_TASK, api::addTask)

fun Route.markAsToDo(api: ToDoApiController) = postEndpoint(ApiPath.MARK_AS_TO_DO, api::markAsToDo)
