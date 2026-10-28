package com.marzec.cheatday

import com.marzec.Api
import com.marzec.cheatday.domain.toDto
import com.marzec.cheatday.domain.toUpdateWeight
import com.marzec.cheatday.dto.PutWeightDto
import com.marzec.cheatday.dto.WeightDto
import com.marzec.cheatday.dto.toDomain
import com.marzec.extensions.getIntOrThrow
import com.marzec.extensions.serviceCall
import com.marzec.extensions.userIdOrThrow
import com.marzec.fiteo.model.domain.toUpdateCategory
import com.marzec.fiteo.model.http.HttpRequest
import com.marzec.fiteo.model.http.HttpResponse
import kotlinx.serialization.json.JsonElement

class CheatDayController(
    private val cheatDayService: CheatDayService
) {

    fun getWeights(request: HttpRequest<Unit>): HttpResponse<List<WeightDto>> {
        return serviceCall {
            cheatDayService.getWeights(request.userIdOrThrow()).map { it.toDto() }
        }
    }

    fun getWeight(request: HttpRequest<Unit>): HttpResponse<WeightDto> {
        return serviceCall {
            cheatDayService.getWeight(
                request.userIdOrThrow(),
                request.getIntOrThrow(Api.Args.ARG_ID)
            ).toDto()
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
                request.userIdOrThrow(),
                request.getIntOrThrow(Api.Args.ARG_ID)
            ).toDto()
        }
    }

    fun updateWeight(request: HttpRequest<Map<String, JsonElement?>>): HttpResponse<WeightDto> =
        serviceCall {
            cheatDayService.updateWeight(
                request.userIdOrThrow(),
                request.getIntOrThrow(Api.Args.ARG_ID),
                request.data.toUpdateWeight()
            ).toDto()
        }
}
