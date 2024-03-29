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
import com.marzec.todo.model.*

class ToDoApiController(
    private val service: TodoService,
    private val taskConstraints: TaskConstraints
) {

    fun getTasks(request: HttpRequest<Unit>): HttpResponse<List<TaskDto>> = serviceCall {
        service.getTasks(request.userIdOrThrow()).map { it.toDto() }
    }

    fun copyTasks(request: HttpRequest<Unit>): HttpResponse<TaskDto> = serviceCall {
        service.copyTask(
            userId = request.userIdOrThrow(),
            id = request.getIntOrThrow(Api.Args.ARG_ID)
        ).toDto()
    }

    fun addTask(request: HttpRequest<CreateTaskDto>): HttpResponse<TaskDto> = request.serviceCall(
        constraint = taskConstraints.scheduledTaskCanNotHaveParentDuringCreation
    ) {
        service.addTask(
            userId = request.userIdOrThrow(),
            task = request.data.toDomain()
        ).toDto()
    }

    fun updateTask(request: HttpRequest<UpdateTaskDto>): HttpResponse<TaskDto> = request.serviceCall(
        constraints = listOf(
            taskConstraints.taskCantBeOwnParent,
            taskConstraints.scheduledTaskCanNotHaveParentDuringUpdate
        )
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
            taskId = request.getIntOrThrow(Api.Args.ARG_ID),
            removeWithSubtasks = request.removeWithSubtasks(),
        ).toDto()
    }

    fun markAsToDo(request: HttpRequest<MarkAsToDoDto>): HttpResponse<List<TaskDto>> = serviceCall {
        service.markAsToDo(
            userId = request.userIdOrThrow(),
            isToDo = request.data.isToDo,
            taskIds = request.data.taskIds
        ).map { it.toDto() }
    }
}

private fun <T> HttpRequest<T>.removeWithSubtasks(): Boolean =
    queries["removeWithSubtasks"]?.first()?.toBooleanStrictOrNull() ?: false

class TaskConstraints {

    val taskCantBeOwnParent = constraint<UpdateTaskDto>(
        breakingRule = {
            val taskId = getIntOrThrow(Api.Args.ARG_ID)
            val newParentTaskId = data.parentTaskId?.value
            taskId == newParentTaskId
        },
        exception = {
            HttpException("New parent task id couldn't be same as id of updated task", HttpStatus.BAD_REQUEST)
        }
    )

    val scheduledTaskCanNotHaveParentDuringCreation = constraint<CreateTaskDto>(
        breakingRule = {
            data.scheduler != null && data.parentTaskId != null
        },
        exception = {
            HttpException("Scheduled task can't have parent", HttpStatus.BAD_REQUEST)
        }
    )

    val scheduledTaskCanNotHaveParentDuringUpdate = constraint<UpdateTaskDto>(
        breakingRule = {
            data.scheduler?.value != null && data.parentTaskId?.value != null
        },
        exception = {
            HttpException("Scheduled task can't have parent", HttpStatus.BAD_REQUEST)
        }
    )
}
