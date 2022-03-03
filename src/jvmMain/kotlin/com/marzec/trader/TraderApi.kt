package com.marzec.trader

import com.marzec.common.deleteByIdEndpoint
import com.marzec.common.getAllEndpoint
import com.marzec.common.postEndpoint
import com.marzec.common.updateByIdEndpoint
import com.marzec.di.Di
import io.ktor.auth.*
import io.ktor.routing.*

fun Route.traderApi(di: Di, api: TraderApiController) {
    authenticate(di.authToken) {
        updatePaper(api)
        removePaper(api)
        papers(api)
        addPaper(api)

        updateTransaction(api)
        removeTransaction(api)
        transactions(api)
        addTransaction(api)
    }
}

fun Route.updatePaper(api: TraderApiController) = updateByIdEndpoint(ApiPath.UPDATE_PAPERS, api::updatePaper)

fun Route.removePaper(api: TraderApiController) = deleteByIdEndpoint(ApiPath.DELETE_PAPERS, api::removePaper)

fun Route.papers(api: TraderApiController) = getAllEndpoint(ApiPath.PAPERS, api::getPapers)

fun Route.addPaper(api: TraderApiController) = postEndpoint(ApiPath.ADD_PAPER, api::addPaper)

fun Route.updateTransaction(api: TraderApiController) = updateByIdEndpoint(ApiPath.UPDATE_TRANSACTIONS, api::updateTransaction)

fun Route.removeTransaction(api: TraderApiController) = deleteByIdEndpoint(ApiPath.DELETE_TRANSACTIONS, api::removeTransaction)

fun Route.transactions(api: TraderApiController) = getAllEndpoint(ApiPath.TRANSACTIONS, api::getTransactions)

fun Route.addTransaction(api: TraderApiController) = postEndpoint(ApiPath.ADD_TRANSACTIONS, api::addTransaction)
