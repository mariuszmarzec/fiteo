package com.marzec.trader

import com.marzec.di.Di
import io.ktor.auth.*
import io.ktor.routing.*

fun Route.traderApi(di: Di, traderApiController: TraderApiController) {
    authenticate(di.authToken) {
    }
}
