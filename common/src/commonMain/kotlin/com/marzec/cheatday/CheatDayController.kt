package com.marzec.cheatday

import com.marzec.cheatday.domain.toDto
import com.marzec.cheatday.dto.PutWeightDto
import com.marzec.cheatday.dto.WeightDto
import com.marzec.cheatday.dto.toDomain
import com.marzec.extensions.getIntOrThrow
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

    fun putWeight(request: HttpRequest<PutWeightDto>): HttpResponse<WeightDto> {
        return serviceCall {
            cheatDayService.putWeight(
                    request.userIdOrThrow(),
                    request.data.value,
                    request.data.date
            ).toDto()
        }
    }

    fun removeWeight(request: HttpRequest<Unit>): HttpResponse<WeightDto> {
        return serviceCall {
            cheatDayService.removeWeight(
                    request.getIntOrThrow(ApiPath.ARG_USER_ID),
                    request.getIntOrThrow(ApiPath.ARG_ID)
            ).toDto()
        }
    }

    fun updateWeight(request: HttpRequest<WeightDto>): HttpResponse<WeightDto> {
        return serviceCall {
            cheatDayService.updateWeight(
                    request.getIntOrThrow(ApiPath.ARG_USER_ID),
                    request.data.toDomain()
            ).toDto()
        }
    }
}