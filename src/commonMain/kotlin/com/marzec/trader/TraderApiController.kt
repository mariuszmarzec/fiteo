package com.marzec.trader

import com.marzec.Api
import com.marzec.extensions.getIntOrThrow
import com.marzec.extensions.serviceCall
import com.marzec.extensions.userIdOrThrow
import com.marzec.fiteo.model.http.HttpRequest
import com.marzec.fiteo.model.http.HttpResponse
import com.marzec.trader.dto.PaperDto
import com.marzec.trader.dto.TransactionDto
import com.marzec.trader.model.toDomain
import com.marzec.trader.model.toDto

class TraderApiController(
    private val service: TraderService
) {

    fun getPapers(request: HttpRequest<Unit>): HttpResponse<List<PaperDto>> = serviceCall {
        service.getPapers().map { it.toDto() }
    }

    fun addPaper(request: HttpRequest<PaperDto>): HttpResponse<PaperDto> = serviceCall {
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
