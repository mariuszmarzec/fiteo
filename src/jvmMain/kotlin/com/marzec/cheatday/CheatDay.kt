package com.marzec.cheatday

import com.marzec.common.deleteByIdEndpoint
import com.marzec.common.getAllEndpoint
import com.marzec.common.postEndpoint
import com.marzec.common.updateByIdEndpoint
import com.marzec.di.Di
import io.ktor.auth.authenticate
import io.ktor.routing.Route

fun Route.cheatDayApi(
    di: Di,
    cheatDayApi: CheatDayController
) {
    authenticate(di.authToken) {
        weights(cheatDayApi)
        putWeight(cheatDayApi)
        removeWeight(cheatDayApi)
        updateWeight(cheatDayApi)
    }
}

fun Route.weights(api: CheatDayController) = getAllEndpoint(ApiPath.WEIGHTS, api::getWeights)

fun Route.putWeight(api: CheatDayController) = postEndpoint(ApiPath.WEIGHT, api::putWeight)

fun Route.removeWeight(api: CheatDayController) = deleteByIdEndpoint(ApiPath.REMOVE_WEIGHT, api::removeWeight)

fun Route.updateWeight(api: CheatDayController) = updateByIdEndpoint(ApiPath.UPDATE_WEIGHT, api::updateWeight)
