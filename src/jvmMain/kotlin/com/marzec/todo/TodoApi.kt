package com.marzec.todo

import com.marzec.common.createHttpRequest
import com.marzec.common.respond
import com.marzec.di.Di
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.routing.*

fun Route.todoApi(di: Di, todoController: ToDoApiController) {
    authenticate(di.authToken) {
        tasks(todoController)
        copyTask(todoController)
        markAsToDo(todoController)
        addTask(todoController)
        updateTask(todoController)
        removeTask(todoController)
        leaveShare(todoController)
    }
}

fun Route.updateTask(api: ToDoApiController) = patch(ApiPath.UPDATE_TASK) {
    respond(api.updateTask(createHttpRequest(call.receive())))
}

fun Route.removeTask(api: ToDoApiController) = delete(ApiPath.DELETE_TASK) {
    respond(api.removeTask(createHttpRequest(Unit)))
}

fun Route.tasks(api: ToDoApiController) = get(ApiPath.TASKS) {
    respond(api.getTasks(createHttpRequest(Unit)))
}

fun Route.copyTask(api: ToDoApiController) = get(ApiPath.COPY_TASK) {
    respond(api.copyTasks(createHttpRequest(Unit)))
}

fun Route.addTask(api: ToDoApiController) = post(ApiPath.ADD_TASK) {
    respond(api.addTask(createHttpRequest(call.receive())))
}

fun Route.markAsToDo(api: ToDoApiController) = post(ApiPath.MARK_AS_TO_DO) {
    respond(api.markAsToDo(createHttpRequest(call.receive())))
}

fun Route.leaveShare(api: ToDoApiController) = post(ApiPath.LEAVE_SHARE) {
    respond(api.leaveShare(createHttpRequest(call.receive())))
}
