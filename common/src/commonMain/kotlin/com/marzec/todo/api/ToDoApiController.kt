package com.marzec.todo.api

import com.marzec.extensions.serviceCall
import com.marzec.extensions.userIdOrThrow
import com.marzec.model.http.HttpRequest
import com.marzec.model.http.HttpResponse
import com.marzec.todo.dto.ToDoListDto
import com.marzec.todo.model.toDto

class ToDoApiController(
        private val service: TodoService
) {
    fun getLists(request: HttpRequest<Unit>): HttpResponse<List<ToDoListDto>> {
        return serviceCall {
            service.getLists(request.userIdOrThrow()).map { it.toDto() }
        }
    }
}