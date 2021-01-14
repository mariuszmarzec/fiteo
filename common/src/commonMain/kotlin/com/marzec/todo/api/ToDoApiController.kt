package com.marzec.todo.api

import com.marzec.ApiPath
import com.marzec.exceptions.HttpException
import com.marzec.exceptions.HttpStatus
import com.marzec.extensions.constraint
import com.marzec.extensions.getIntOrThrow
import com.marzec.extensions.serviceCall
import com.marzec.extensions.userIdOrThrow
import com.marzec.model.http.HttpRequest
import com.marzec.model.http.HttpResponse
import com.marzec.todo.dto.CreateTodoListDto
import com.marzec.todo.dto.TaskDto
import com.marzec.todo.dto.ToDoListDto
import com.marzec.todo.model.CreateTaskDto
import com.marzec.todo.model.UpdateTask
import com.marzec.todo.model.UpdateTaskDto
import com.marzec.todo.model.toDomain
import com.marzec.todo.model.toDto

class ToDoApiController(
        private val service: TodoService
) {
    fun getLists(request: HttpRequest<Unit>): HttpResponse<List<ToDoListDto>> = serviceCall {
        service.getLists(request.userIdOrThrow()).map { it.toDto() }
    }

    fun addList(request: HttpRequest<CreateTodoListDto>): HttpResponse<ToDoListDto> = serviceCall {
        service.addList(
                request.userIdOrThrow(),
                request.data.title
        ).toDto()
    }

    fun removeList(request: HttpRequest<Unit>): HttpResponse<ToDoListDto> = serviceCall {
        service.removeList(
                request.userIdOrThrow(),
                request.getIntOrThrow(ApiPath.ARG_ID)
        ).toDto()
    }

    fun addTask(request: HttpRequest<CreateTaskDto>): HttpResponse<TaskDto> = serviceCall {
        service.addTask(
                userId = request.userIdOrThrow(),
                listId = request.getIntOrThrow(ApiPath.ARG_ID),
                task = request.data.toDomain()
        ).toDto()
    }

    fun updateTask(request: HttpRequest<UpdateTaskDto>): HttpResponse<TaskDto> {
        return request.serviceCall(
                constraint = TaskConstraints.taskCantBeOwnParent
        ) {
            service.updateTask(
                    userId = request.userIdOrThrow(),
                    taskId = request.getIntOrThrow(ApiPath.ARG_ID),
                    task = request.data.toDomain()
            ).toDto()
        }
    }

    fun removeTask(request: HttpRequest<Unit>): HttpResponse<TaskDto> = serviceCall {
        service.removeTask(
                userId = request.userIdOrThrow(),
                taskId = request.getIntOrThrow(ApiPath.ARG_ID)
        ).toDto()
    }
}

private object TaskConstraints {
    val taskCantBeOwnParent = constraint<UpdateTaskDto>(
            check = {
                val taskId = getIntOrThrow(ApiPath.ARG_ID)
                val newParentTaskId = data.parentTaskId
                taskId != newParentTaskId
            },
            exception = {
                HttpException("New parent task id couldn't be same as id of updated task", HttpStatus.BAD_REQUEST)
            }
    )
}