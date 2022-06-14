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
import com.marzec.trader.dto.PaperDto
import com.marzec.trader.dto.PaperTagDto
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

    fun getPaperTags(request: HttpRequest<Unit>): HttpResponse<List<PaperTagDto>> = serviceCall {
        service.getPaperTags().map { it.toDto() }
    }

    fun addPaperTag(request: HttpRequest<PaperTagDto>): HttpResponse<PaperTagDto> = serviceCall {
        service.addPaperTag(
            tag = request.data.toDomain()
        ).toDto()
    }

    fun updatePaperTag(request: HttpRequest<PaperTagDto>): HttpResponse<PaperTagDto> = serviceCall {
        service.updatePaperTag(
            tag = request.data.toDomain()
        ).toDto()
    }

    fun removePaperTag(request: HttpRequest<Unit>): HttpResponse<PaperTagDto> = serviceCall {
        service.removePaperTag(
            tagId = request.getIntOrThrow(Api.Args.ARG_ID)
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
        breakingRule = {
            if (data.toDomain().type == PaperType.SETTLEMENT_CURRENCY) {
                service.getPapers().count { it.type == PaperType.SETTLEMENT_CURRENCY } > 0
            } else {
                false
            }
        },
        exception = {
            HttpException("There could be only one settlement currency", HttpStatus.BAD_REQUEST)
        }
    )
}
