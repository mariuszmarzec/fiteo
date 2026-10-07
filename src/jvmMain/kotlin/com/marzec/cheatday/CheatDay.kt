package com.marzec.cheatday

import com.marzec.common.*
import com.marzec.di.Di
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route

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

fun Route.weights(api: CheatDayController) = getAllEndpoint(ApiPath.WEIGHTS, api::getWeights)

fun Route.weight(api: CheatDayController) = getByIdEndpoint(ApiPath.WEIGHT_BY_ID, api::getWeight)

fun Route.putWeight(api: CheatDayController) = postEndpoint(ApiPath.WEIGHTS, api::putWeight)

fun Route.removeWeight(api: CheatDayController) = deleteByIdEndpoint(ApiPath.WEIGHT_BY_ID, api::removeWeight)

fun Route.updateWeight(api: CheatDayController) = updateByIdEndpoint(ApiPath.WEIGHT_BY_ID, api::updateWeight)
