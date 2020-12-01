package com.marzec.cheatday

import com.marzec.cheatday.domain.toDto
import com.marzec.cheatday.dto.WeightDto
import com.marzec.extensions.serviceCall
import com.marzec.extensions.userIdOrThrow
import com.marzec.model.http.HttpRequest
import com.marzec.model.http.HttpResponse

class CheatDayController(
        private val cheatDayService: CheatDayService
) {

    fun getWeights(request: HttpRequest<Unit>): HttpResponse<List<WeightDto>> {
        return serviceCall {
            cheatDayService.getWeights(request.userIdOrThrow()).map { it.toDto() }
        }
    }
}