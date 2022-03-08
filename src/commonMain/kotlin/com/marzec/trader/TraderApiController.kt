package com.marzec.trader

import com.marzec.Api
import com.marzec.exceptions.HttpException
import com.marzec.exceptions.HttpStatus
import com.marzec.extensions.constraint
import com.marzec.extensions.getIntOrThrow
import com.marzec.extensions.serviceCall
import com.marzec.extensions.userIdOrThrow
import com.marzec.fiteo.model.http.HttpRequest
import com.marzec.fiteo.model.http.HttpResponse
import com.marzec.todo.model.UpdateTaskDto
import com.marzec.trader.dto.PaperDto
import com.marzec.trader.dto.TransactionDto
import com.marzec.trader.model.PaperType
import com.marzec.trader.model.toDomain
import com.marzec.trader.model.toDto

class TraderApiController(
    private val service: TraderService,
    private val traderConstraints: TraderConstraints
) {

    fun getPapers(request: HttpRequest<Unit>): HttpResponse<List<PaperDto>> = serviceCall {
        service.getPapers().map { it.toDto() }
    }

    fun addPaper(request: HttpRequest<PaperDto>): HttpResponse<PaperDto> = request.serviceCall(
        constraint = traderConstraints.onlyOneSettlementCurrency
    ) {
        service.addPaper(
            paper = request.data.toDomain()
        ).toDto()
    }

    fun updatePaper(request: HttpRequest<PaperDto>): HttpResponse<PaperDto> = serviceCall {
        service.updatePaper(
            paper = request.data.toDomain()
        ).toDto()
    }

    fun removePaper(request: HttpRequest<Unit>): HttpResponse<PaperDto> = serviceCall {
        service.removePaper(
            paperId = request.getIntOrThrow(Api.Args.ARG_ID)
        ).toDto()
    }

    fun getTransactions(request: HttpRequest<Unit>): HttpResponse<List<TransactionDto>> = serviceCall {
        service.getTransactions(request.userIdOrThrow()).map { it.toDto() }
    }

    fun addTransaction(request: HttpRequest<TransactionDto>): HttpResponse<TransactionDto> = serviceCall {
        service.addTransaction(
            userId = request.userIdOrThrow(),
            transaction = request.data.toDomain()
        ).toDto()
    }

    fun updateTransaction(request: HttpRequest<TransactionDto>): HttpResponse<TransactionDto> = serviceCall {
        service.updateTransaction(
            userId = request.userIdOrThrow(),
            transactionId = request.getIntOrThrow(Api.Args.ARG_ID),
            transaction = request.data.toDomain()
        ).toDto()
    }

    fun removeTransaction(request: HttpRequest<Unit>): HttpResponse<TransactionDto> = serviceCall {
        service.removeTransaction(
            userId = request.userIdOrThrow(),
            transactionId = request.getIntOrThrow(Api.Args.ARG_ID)
        ).toDto()
    }
}

class TraderConstraints(
    private val service: TraderService
) {
    val onlyOneSettlementCurrency = constraint<PaperDto>(
        check = {
            if (data.toDomain().type == PaperType.SETTLEMENT_CURRENCY) {
                service.getPapers().count { it.type == PaperType.SETTLEMENT_CURRENCY } == 0
            } else {
                true
            }
        },
        exception = {
            HttpException("There could be only one settlement currency", HttpStatus.BAD_REQUEST)
        }
    )
}
