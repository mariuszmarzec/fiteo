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
        putWeightDeprecated(cheatDayApi)
        putWeight(cheatDayApi)
        removeWeightDeprecated(cheatDayApi)
        updateWeightDeprecated(cheatDayApi)
        removeWeight(cheatDayApi)
        updateWeight(cheatDayApi)
    }
}

fun Route.weights(api: CheatDayController) = getAllEndpoint(ApiPath.WEIGHTS, api::getWeights)

fun Route.weight(api: CheatDayController) = getByIdEndpoint(ApiPath.WEIGHT_BY_ID, api::getWeight)

@Deprecated("")
fun Route.putWeightDeprecated(api: CheatDayController) = postEndpoint(ApiPath.WEIGHT, api::putWeight)

fun Route.putWeight(api: CheatDayController) = postEndpoint(ApiPath.WEIGHTS, api::putWeight)

@Deprecated("")
fun Route.removeWeightDeprecated(api: CheatDayController) = deleteByIdEndpoint(ApiPath.REMOVE_WEIGHT_DEPRECATED, api::removeWeight)

@Deprecated("")
fun Route.updateWeightDeprecated(api: CheatDayController) = updateByIdEndpoint(ApiPath.UPDATE_WEIGHT_DEPRECATED, api::updateWeight)

fun Route.removeWeight(api: CheatDayController) = deleteByIdEndpoint(ApiPath.WEIGHT_BY_ID, api::removeWeight)

fun Route.updateWeight(api: CheatDayController) = updateByIdEndpoint(ApiPath.WEIGHT_BY_ID, api::updateWeight)
