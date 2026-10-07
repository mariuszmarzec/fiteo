package com.marzec.cheatday

import com.marzec.common.createHttpRequest
import com.marzec.common.receiveHttpRequest
import com.marzec.common.respond
import com.marzec.di.Di
import com.marzec.cheatday.dto.PutWeightDto
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.routing.*
import kotlinx.serialization.json.JsonElement

fun Route.cheatDayApi(
    di: Di,
    cheatDayApi: CheatDayController
) {
    authenticate(di.authToken) {
        weights(cheatDayApi)
        weight(cheatDayApi)
        putWeight(cheatDayApi)
        removeWeight(cheatDayApi)
        updateWeight(cheatDayApi)
    }
}

fun Route.weights(api: CheatDayController) = get(ApiPath.WEIGHTS) {
    respond(api.getWeights(createHttpRequest(Unit)))
}

fun Route.weight(api: CheatDayController) = get(ApiPath.WEIGHT_BY_ID) {
    respond(api.getWeight(createHttpRequest(Unit)))
}

fun Route.putWeight(api: CheatDayController) = post(ApiPath.WEIGHTS) {
    respond(api.putWeight(receiveHttpRequest<PutWeightDto>()))
}

fun Route.removeWeight(api: CheatDayController) = delete(ApiPath.WEIGHT_BY_ID) {
    respond(api.removeWeight(createHttpRequest(Unit)))
}

fun Route.updateWeight(api: CheatDayController) = patch(ApiPath.WEIGHT_BY_ID) {
    respond(api.updateWeight(receiveHttpRequest<Map<String, JsonElement?>>()))
}
