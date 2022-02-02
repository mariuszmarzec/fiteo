package com.marzec.todo

import com.marzec.Api
import com.marzec.exceptions.HttpException
import com.marzec.exceptions.HttpStatus
import com.marzec.extensions.constraint
import com.marzec.extensions.getIntOrThrow
import com.marzec.extensions.serviceCall
import com.marzec.extensions.userIdOrThrow
import com.marzec.fiteo.model.http.HttpRequest
import com.marzec.fiteo.model.http.HttpResponse
import com.marzec.todo.dto.TaskDto
import com.marzec.todo.model.CreateTaskDto
import com.marzec.todo.model.UpdateTaskDto
import com.marzec.todo.model.toDomain
import com.marzec.todo.model.toDto

class ToDoApiController(
    private val service: TodoService
) {

    fun getTasks(request: HttpRequest<Unit>): HttpResponse<List<TaskDto>> = serviceCall {
        service.getTasks(request.userIdOrThrow()).map { it.toDto() }
    }

    fun addTask(request: HttpRequest<CreateTaskDto>): HttpResponse<TaskDto> = serviceCall {
        service.addTask(
            userId = request.userIdOrThrow(),
            task = request.data.toDomain()
        ).toDto()
    }

    fun updateTask(request: HttpRequest<UpdateTaskDto>): HttpResponse<TaskDto> = request.serviceCall(
        constraint = TaskConstraints.taskCantBeOwnParent
    ) {
        service.updateTask(
            userId = request.userIdOrThrow(),
            taskId = request.getIntOrThrow(Api.Args.ARG_ID),
            task = request.data.toDomain()
        ).toDto()
    }

    fun removeTask(request: HttpRequest<Unit>): HttpResponse<TaskDto> = serviceCall {
        service.removeTask(
            userId = request.userIdOrThrow(),
            taskId = request.getIntOrThrow(Api.Args.ARG_ID)
        ).toDto()
    }
}

private object TaskConstraints {
    val taskCantBeOwnParent = constraint<UpdateTaskDto>(
        check = {
            val taskId = getIntOrThrow(Api.Args.ARG_ID)
            val newParentTaskId = data.parentTaskId
            taskId != newParentTaskId
        },
        exception = {
            HttpException("New parent task id couldn't be same as id of updated task", HttpStatus.BAD_REQUEST)
        }
    )
}
